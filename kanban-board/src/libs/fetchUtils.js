//function ที่ติดต่อ back-end.
let token = undefined

function getToken() {
  token = sessionStorage.getItem("token")
}

//เช็คว่า Token หมดอายุ
function isTokenExpired(token) {
  if (!token) return true

  const tokenParts = token.split(".")
  if (tokenParts.length !== 3) return true

  const base64Url = tokenParts[1]
  const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/")
  const jsonPayload = decodeURIComponent(
    atob(base64)
      .split("")
      .map(function (c) {
        return "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2)
      })
      .join("")
  )

  const { exp } = JSON.parse(jsonPayload)

  // ตรวจสอบว่าเวลาปัจจุบันเกินเวลาหมดอายุของ token หรือไม่
  const currentTime = Math.floor(Date.now() / 1000)
  return currentTime > exp
}

// ฟังก์ชันเช็คและรีเฟรช Token หากหมดอายุ
async function checkAndRefreshToken(url, tokenExpired, refreshToken) {
  // ตรวจสอบว่า token หมดอายุหรือไม่
  if (isTokenExpired(tokenExpired)) {
    console.log("Token หมดอายุแล้ว")
    try {
      const res = await fetch(`${url}`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${refreshToken}`,
        },
      })

      const statusCode = res.status

      if (statusCode === 401) {
        console.log("Token หมดอายุแล้วจากเซิร์ฟเวอร์, statusCode 401")
        return { statusCode: 401, accessNewToken: null }
      }

      // รับ status code และ token ใหม่ (หากสำเร็จ)
      if (statusCode === 200) {
        const data = await res.json()
        const accessNewToken = data.access_token

        console.log("new token:", accessNewToken)
        console.log("status code:", statusCode)

        return { statusCode, accessNewToken }
      }
    } catch (error) {
      console.error("เกิดข้อผิดพลาดในการรีเฟรช token:", error)
      return { statusCode: 500, accessNewToken: null } // คืนค่า statusCode 500 เมื่อเกิดข้อผิดพลาด
    }
  } else {
    return { statusCode: 200, accessNewToken: tokenExpired } // คืนค่า statusCode 500 เมื่อเกิดข้อผิดพลาด
  }
}

async function getItems(url) {
  getToken()
  let data
  try {
    data = await fetch(url, {
      //GET Method
      method: "GET",
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })

    if (data.status === 401) {
      throw new Error("Unauthorized") // คุณสามารถปรับข้อความ error ได้
    }

    const items = await data.json()
    return items
  } catch (error) {
    if (data.status === 404) return 404
    if (data.status === 401) return 401
    if (data.status === 400) return 400
  }
}

async function getStatusLimits(url) {
  let data
  try {
    data = await fetch(`${url}/maximum-task`, {
      //GET Method
      method: "GET",
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })
    const items = await data.json()
    return items
  } catch (error) {
    if (data.status === 404) return 404
    if (data.status === 401) return 401
    if (data.status === 400) return 400
  }
}

async function getItemById(url, id) {
  let data
  try {
    data = await fetch(`${url}/${id}`, {
      //GET Method
      method: "GET",
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })

    if (data.status === 401) {
      throw new Error("Unauthorized") // คุณสามารถปรับข้อความ error ได้
    }

    const item = await data.json()
    return item
  } catch (error) {
    // return { error: error.message }

    if (data.status === 404) return 404
    if (data.status === 401) return 401
  }
}

async function findStatus(url, id) {
  let data
  try {
    data = await fetch(`${url}/${id}`, {
      //GET Method
      method: "GET",
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })
    return data.status
  } catch (error) {
    if (data.status === 404) return 404
  }
}

async function deleteItemById(url, id) {
  //DELETE Method
  getToken()
  try {
    const res = await fetch(`${url}/${id}`, {
      method: "DELETE",
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })
    return res.status
  } catch (error) {}
}

async function deleteItemByIdToNewId(url, oldId, newId) {
  //DELETE Method
  getToken()
  try {
    const res = await fetch(`${url}/${oldId}/${newId}`, {
      method: "DELETE",
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })
    return res.status
  } catch (error) {}
}

async function addItem(url, newItem) {
  getToken()
  try {
    const res = await fetch(url, {
      method: "POST",
      headers: {
        "content-type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({
        //destrucuring
        ...newItem,
      }),
    })
    // Get the HTTP status code
    const statusCode = res.status
    const newTask = await res.json()

    // Return both the added item and the status code
    return { newTask, statusCode }
  } catch (error) {}
}

async function editItem(url, id, editItem) {
  getToken()
  try {
    const res = await fetch(`${url}/${id}`, {
      method: "PUT",
      headers: {
        "content-type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(editItem),
    })
    const statusCode = res.status
    const editedItem = await res.json()
    return { editedItem, statusCode }
  } catch (error) {}
}

async function editLimitStatus(url, boolean, maxLimit) {
  getToken()
  try {
    const res = await fetch(
      `${url}/maximum-task?taskLimitEnabled=${boolean}&maxTasksPerStatus=${maxLimit}`,
      {
        method: "PATCH",
        headers: {
          "content-type": "application/json",
          Authorization: `Bearer ${token}`,
        },
      }
    )
    const editedLimit = await res.json()
    const status = res.status
    return { editedLimit, status }
  } catch (error) {}
}

// Function to handle login
async function login(url, userName, password) {
  try {
    const res = await fetch(`${url}`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        userName: userName,
        password: password,
      }),
    })

    // Return status code for further processing
    const data = await res.json()
    const token = data

    return { res, token }
  } catch (error) {
    return 500 // Internal server error
  }
}

export {
  getItems,
  getItemById,
  deleteItemById,
  addItem,
  editItem,
  deleteItemByIdToNewId,
  findStatus,
  getStatusLimits,
  editLimitStatus,
  login,
  getToken,
  isTokenExpired,
  checkAndRefreshToken,
}
