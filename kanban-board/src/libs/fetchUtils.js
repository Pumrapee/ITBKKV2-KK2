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

async function deleteItemById(url, id) {
  //DELETE Method
  try {
    const res = await fetch(`${url}/${id}`, {
      method: "DELETE",
    })
    return res.status
  } catch (error) {
    console.log(`error: ${error}`)
  }
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
    const addedItem = await res.json()
    return addedItem
  } catch (error) {
    console.log(`error: ${error}`)
  }
}

async function editItem(url, id, editItem) {
  try {
    const res = fetch(`${url}/${id}`, {
      method: "PUT",
      headers: {
        "content-type": "application/json",
      },
      body: JSON.stringify({
        ...editItem,
      }),
    })
    const editedItem = await res.json()
    return editedItem
  } catch (error) {
    console.log(`error: ${error}`)
  }
}
export { getItems, getItemById, deleteItemById, addItem, editItem }
