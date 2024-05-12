//function ที่ติดต่อ back-end
async function getItems(url) {
  try {
    const data = await fetch(url) //GET Method
    const items = await data.json()
    return items
  } catch (error) {}
}
async function getItemById(url, id) {
  let data
  try {
    data = await fetch(`${url}/${id}`)
    const item = await data.json()
    return item
  } catch (error) {
    if (data.status === 404) return 404
  }
}
async function findStatus(url, id) {
  let data
  try {
    data = await fetch(`${url}/${id}`)
    const item = await data.json()
    return item
  } catch (error) {
    if (data.status === 404) return 404
  }
}

async function deleteItemById(url, id) {
  //DELETE Method
  try {
    const res = await fetch(`${url}/${id}`, {
      method: "DELETE",
    })
    return res.status
  } catch (error) {}
}

async function deleteItemByIdToNewId(url, oldId, newId) {
  //DELETE Method
  try {
    const res = await fetch(`${url}/${oldId}/${newId}`, {
      method: "DELETE",
    })
    return res.status
  } catch (error) {}
}

async function addItem(url, newItem) {
  try {
    const res = await fetch(url, {
      method: "POST",
      headers: {
        "content-type": "application/json",
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
  try {
    const res = await fetch(`${url}/${id}`, {
      method: "PUT",
      headers: {
        "content-type": "application/json",
      },
      body: JSON.stringify(editItem),
    })
    const statusCode = res.status

    const editedItem = await res.json()
    return { editedItem, statusCode }
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
}
