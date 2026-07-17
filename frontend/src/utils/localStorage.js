/**
 * localStorage 工具封装
 */
export const setStorage = (name, data) => {
  localStorage.setItem(name, data)
}

export const getStorage = (name) => {
  return localStorage.getItem(name)
}

export const delStorage = (name) => {
  localStorage.removeItem(name)
}

export const clearStorage = () => {
  localStorage.clear()
}
