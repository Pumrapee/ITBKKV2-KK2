//function ที่ติดต่อ back-end.
let token = undefined

function getToken() {
  token = localStorage.getItem("token")
}

async function getItems(url) {
  console.log(token)
  try {
    const data = await fetch(url, {
      //GET Method
      method: "GET",
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })
    const items = await data.json()
    return items
  } catch (error) {}
}

async function getStatusLimits(url) {
  try {
    const data = await fetch(`${url}/maximum-task`, {
      //GET Method
      method: "GET",
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })
    const items = await data.json()
    return items
  } catch (error) {}
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
    const item = await data.json()
    return item
  } catch (error) {
    if (data.status === 404) return 404
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
  console.log(newItem)
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
    console.log(newTask)
    // Return both the added item and the status code
    return { newTask, statusCode }
  } catch (error) {}
}

async function editItem(url, id, editItem) {
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
    return editedLimit
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
    const token = data.access_token

    return { res, token }
  } catch (error) {
    console.error("Error during login:", error)
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
}
