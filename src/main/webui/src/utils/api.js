// Fetch Wrapper intercetando token automaticamente
export async function apiFetch(url, options = {}) {
  const token = localStorage.getItem('token');
  
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
    ...(token ? { Authorization: `Bearer ${token}` } : {})
  };

  const response = await fetch(url, { ...options, headers });
  
  // Tratar Unauthorized globalmente
  if (response.status === 401) {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = '/login';
    throw new Error('Sessão expirada. Faça login novamente.');
  }

  // Mesmo sendo status de falha (400, 500), tenta decodificar JSON se houver
  let data = null;
  try {
    data = await response.json();
  } catch (e) {
    // a resposta não tinha json
  }

  if (!response.ok) {
    const errorMsg = (data && data.message) ? data.message : 'Erro na requisição. Status: ' + response.status;
    throw new Error(errorMsg);
  }

  return data;
}
