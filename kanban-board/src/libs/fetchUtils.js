import router from "@/router"

//function ที่ติดต่อ back-end.
let tokenStorage = undefined
let refresh_token = localStorage.getItem("refreshToken")

function getToken() {
  tokenStorage = localStorage.getItem("token")
}

function tokenIsNull(token) {
  // return token ? { Authorization: `Bearer ${token}` } : {}
  return !token || token === "null" || token === null || token === ""
    ? {}
    : { Authorization: `Bearer ${token}` }
}

//เช็คว่า Token หมดอายุ
function isTokenExpired(token) {
  // if (!token) return true

  // if (!token) {
  //   return false
  // }

  // Validate the token format
  if (
    (!token && !refresh_token) ||
    (token === "null" && !refresh_token) ||
    token === "null"
  ) {
    return false
  }

  // if (token === "null") {
  //   return false
  // }

  const tokenParts = token?.split(".")
  if (tokenParts?.length !== 3) return true

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

  if (isTokenExpired(tokenStorage)) {
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
        return { statusCode: 401, accessNewToken: null }
      }

      // รับ status code และ token ใหม่ (หากสำเร็จ)
      if (statusCode === 200) {
        const data = await res.json()
        const accessNewToken = data.access_token

        return { statusCode, accessNewToken }
      }
    } catch (error) {
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
      // headers: {
      //   Authorization: `Bearer ${tokenStorage}`,
      // },
      headers: tokenIsNull(tokenStorage),
    })

    if (data.status === 401) {
      throw new Error("Unauthorized") // คุณสามารถปรับข้อความ error ได้
    }

    // if (data.status === 403) {
    //   router.push({ name: "forbidden" })
    // }

    const items = await data.json()
    return items
  } catch (error) {
    if (data.status === 404) return 404
    if (data.status === 401) return 401
    if (data.status === 400) return 400
    if (data.status === 403) return 403
  }
}

async function getStatusLimits(url) {
  let data
  try {
    data = await fetch(`${url}/maximum-task`, {
      //GET Method
      method: "GET",
      // headers: {
      //   Authorization: `Bearer ${tokenStorage}`,
      // },
      headers: tokenIsNull(tokenStorage),
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
      // headers: {
      //   Authorization: `Bearer ${tokenStorage}`,
      // },
      headers: tokenIsNull(tokenStorage),
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
      // headers: {
      //   Authorization: `Bearer ${tokenStorage}`,
      // },
      headers: tokenIsNull(tokenStorage),
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
        Authorization: `Bearer ${tokenStorage}`,
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
        Authorization: `Bearer ${tokenStorage}`,
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
        Authorization: `Bearer ${tokenStorage}`,
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
        Authorization: `Bearer ${tokenStorage}`,
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
          Authorization: `Bearer ${tokenStorage}`,
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

async function Visibility(url, boardId, newVisibility) {
  getToken()
  try {
    const res = await fetch(`${url}/${boardId}`, {
      method: "PATCH",
      headers: {
        "content-type": "application/json",
        Authorization: `Bearer ${tokenStorage}`,
      },
      body: JSON.stringify({
        visibility: newVisibility,
      }),
    })
    const statusCode = res.status
    let responseBody

    if (res.status === 200) {
      responseBody = await res.json()
    }

    return { responseBody, statusCode }
  } catch (error) {
    console.error("Error in patching visibility", error)
  }
}

async function getBoardItems(url) {
  getToken()

  // Check if the token is present
  if (!tokenStorage) {
    return // Exit the function if there is no token
  }

  const headers = { Authorization: `Bearer ${tokenStorage}` } // Set headers with the token
  try {
    const response = await fetch(url, { headers })
    const items = await response.json()
    return items
  } catch (error) {}
}

async function patchItem(url, newItem) {
  getToken()
  try {
    const res = await fetch(url, {
      method: "PATCH",
      headers: {
        "content-type": "application/json",
        Authorization: `Bearer ${tokenStorage}`,
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
  Visibility,
  checkAndRefreshToken,
  getBoardItems,
  patchItem,
}
