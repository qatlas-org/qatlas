const KEY = 'qatlas_current_project_id';

export function getCurrentProjectId(): number | null {
  const raw = localStorage.getItem(KEY);
  return raw ? Number(raw) : null;
}

export function setCurrentProjectId(id: number): void {
  localStorage.setItem(KEY, String(id));
}
