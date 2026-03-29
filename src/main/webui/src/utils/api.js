// Fetch Wrapper intercetando token automaticamente
export async function apiFetch(url, options = {}) {
  const token = localStorage.getItem('token');
  
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
    ...(token && token !== 'undefined' && token !== 'null' ? { Authorization: `Bearer ${token}` } : {})
  };

  // Garante que a base url correta seja usada (extraída dinamicamente do browser)
  // Como usamos Hash History, o pathname será sempre o caminho real do app
  const basePath = window.location.pathname;
  const prefix = basePath.endsWith('/') ? basePath.slice(0, -1) : basePath;
  
  // Normaliza a URL. Se for ./api ou /api, lidamos corretamente
  const cleanUrl = url.replace(/^\.?\/?/, '/');
  const finalUrl = `${prefix}${cleanUrl}`;

  console.log(`[apiFetch] Calling ${finalUrl}`);
  console.log(`[apiFetch] Token validation: exists? ${!!token}, valid? ${token !== 'undefined' && token !== 'null'}`);

  const response = await fetch(finalUrl, { ...options, headers });
  
  // Tratar Unauthorized globalmente
  if (response.status === 401) {
    console.error(`[apiFetch] 401 Unauthorized for ${finalUrl}. Clearing local storage.`);
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = `${prefix}/#/login`;
    throw new Error('Sessão expirada. Faça login novamente.');
  }

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
