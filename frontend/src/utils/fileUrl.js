//const STORAGE_FILE_BASE_URL = "http://89.104.67.75:8765/api/v1/storage/files";
//
///**
// * Возвращает публичный URL для файла, доступный через бэкенд
// * @param {string|null} fileName — например: "avatar/602fb21e-.../photo.png"
// * @returns {string} полный URL или пустая строка
// */
//export function getPublicFileUrl(fileName) {
//  if (!fileName) return "";
//  return `${STORAGE_FILE_BASE_URL}/${encodeURIComponent(fileName)}`;
//}

  const STORAGE_FILE_BASE_URL = "http://213.217.1.240/api/v1/storage/files";

export function getPublicFileUrl(fileName) {
  if (!fileName) return "";
  // Используем query-параметр name
  return `${STORAGE_FILE_BASE_URL}?name=${encodeURIComponent(fileName)}`;
}