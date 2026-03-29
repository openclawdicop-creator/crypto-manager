<template>
  <div class="dashboard-container">

    <!-- Stats Cards -->
    <div class="stats-grid">
      <div v-for="stat in stats" :key="stat.label" class="stat-card glass-card">
        <div class="stat-icon" :style="{ background: stat.color }">
          <span v-html="stat.icon"></span>
        </div>
        <div class="stat-info">
          <span class="stat-label">{{ stat.label }}</span>
          <span class="stat-value">
            <template v-if="loading">
              <span class="skeleton-value"></span>
            </template>
            <template v-else>{{ stat.value }}</template>
          </span>
        </div>
      </div>
    </div>

    <!-- Last Price Check Info -->
    <div class="last-check-banner glass-card">
      <div class="last-check-icon">
        <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="none"
             stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>
        </svg>
      </div>
      <div class="last-check-text">
        <span class="last-check-title">Ultima Consulta de Preco</span>
        <span class="last-check-value" :class="{ 'no-data': !ultimaConsulta }">
          {{ loading ? '...' : (ultimaConsulta ? formatDate(ultimaConsulta.dataHoraConsulta) : 'Nenhuma consulta registrada') }}
        </span>
        <span v-if="ultimaConsulta && !loading" class="last-check-detail">
          {{ ultimaConsulta.parametrizacao?.tokenCompra?.simbolo || '?' }} /
          {{ ultimaConsulta.parametrizacao?.tokenVenda?.simbolo || '?' }}
          via {{ ultimaConsulta.parametrizacao?.exchange?.nome || '?' }}
          - compra: <strong>{{ formatCotacao(ultimaConsulta.cotacaoCompra) }}</strong>
          | venda: <strong>{{ formatCotacao(ultimaConsulta.cotacaoVenda) }}</strong>
        </span>
      </div>
    </div>

    <!-- Recent Price Checks Table -->
    <div class="section glass-card">
      <div class="section-header">
        <h3>Ultimas 10 Consultas de Preco</h3>
        <span class="badge">{{ recentHistoricos.length }} registros</span>
      </div>

      <div v-if="loading" class="loading-state">
        <span class="spinner"></span> Carregando...
      </div>

      <div v-else-if="recentHistoricos.length === 0" class="empty-state">
        <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none"
             stroke="#cbd5e1" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
          <polyline points="14 2 14 8 20 8"/>
          <line x1="16" y1="13" x2="8" y2="13"/>
          <line x1="16" y1="17" x2="8" y2="17"/>
          <polyline points="10 9 9 9 8 9"/>
        </svg>
        <p>Nenhuma consulta de preco registrada ainda.</p>
      </div>

      <div v-else class="table-responsive">
        <table class="modern-table">
          <thead>
            <tr>
              <th>#</th>
              <th>Par de Tokens</th>
              <th>Exchange</th>
              <th>Cotacao Compra</th>
              <th>Cotacao Venda</th>
              <th>Data / Hora</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="h in recentHistoricos" :key="h.id" class="table-row-anim">
              <td data-label="#">
                <span class="id-badge">#{{ h.id }}</span>
              </td>
              <td data-label="Par de Tokens">
                <span class="token-badge buy">{{ h.parametrizacao?.tokenCompra?.simbolo || '?' }}</span>
                <span class="pair-sep">/</span>
                <span class="token-badge sell">{{ h.parametrizacao?.tokenVenda?.simbolo || '?' }}</span>
              </td>
              <td data-label="Exchange">
                <span class="exchange-name">{{ h.parametrizacao?.exchange?.nome || '-' }}</span>
              </td>
              <td data-label="Cotacao Compra">
                <span class="cotacao">{{ formatCotacao(h.cotacaoCompra) }}</span>
              </td>
              <td data-label="Cotacao Venda">
                <span class="cotacao">{{ formatCotacao(h.cotacaoVenda) }}</span>
              </td>
              <td data-label="Data / Hora">
                <span class="datetime">{{ formatDate(h.dataHoraConsulta) }}</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { apiFetch } from '../utils/api.js'

const loading = ref(true)
const ativos = ref([])
const exchanges = ref([])
const redes = ref([])
const parametrizacoes = ref([])
const historicos = ref([])

const sortedHistoricos = computed(() =>
  [...historicos.value].sort((a, b) => new Date(b.dataHoraConsulta) - new Date(a.dataHoraConsulta))
)

const ultimaConsulta = computed(() => sortedHistoricos.value[0] || null)
const recentHistoricos = computed(() => sortedHistoricos.value.slice(0, 10))

const stats = computed(() => [
  {
    label: 'Ativos Cadastrados',
    value: ativos.value.length,
    icon: '<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="16"/><line x1="8" y1="12" x2="16" y2="12"/></svg>',
    color: 'linear-gradient(135deg, #6366f1, #8b5cf6)'
  },
  {
    label: 'Exchanges Cadastradas',
    value: exchanges.value.length,
    icon: '<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="17 1 21 5 17 9"/><path d="M3 11V9a4 4 0 0 1 4-4h14"/><polyline points="7 23 3 19 7 15"/><path d="M21 13v2a4 4 0 0 1-4 4H3"/></svg>',
    color: 'linear-gradient(135deg, #0ea5e9, #2563eb)'
  },
  {
    label: 'Redes Cadastradas',
    value: redes.value.length,
    icon: '<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 22 8.5 22 15.5 12 22 2 15.5 2 8.5 12 2"/><line x1="12" y1="22" x2="12" y2="15.5"/><polyline points="22 8.5 12 15.5 2 8.5"/></svg>',
    color: 'linear-gradient(135deg, #10b981, #059669)'
  },
  {
    label: 'Parametrizacoes',
    value: parametrizacoes.value.length,
    icon: '<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"/><path d="M19.07 4.93a10 10 0 0 1 0 14.14"/><path d="M4.93 4.93a10 10 0 0 0 0 14.14"/></svg>',
    color: 'linear-gradient(135deg, #f59e0b, #d97706)'
  },
  {
    label: 'Consultas Realizadas',
    value: historicos.value.length,
    icon: '<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>',
    color: 'linear-gradient(135deg, #ef4444, #dc2626)'
  }
])

const formatCotacao = (v) => {
  if (v == null) return '-'
  return new Intl.NumberFormat('pt-BR', {
    style: 'decimal',
    minimumFractionDigits: 2,
    maximumFractionDigits: 8
  }).format(v)
}

const formatDate = (dt) => {
  if (!dt) return '-'
  return new Date(dt).toLocaleString('pt-BR')
}

const safe = (fn) => fn().catch(() => ({ data: [] }))

onMounted(async () => {
  try {
    const [r1, r2, r3, r4, r5] = await Promise.all([
      safe(() => apiFetch('/api/ativos')),
      safe(() => apiFetch('/api/exchanges')),
      safe(() => apiFetch('/api/redes')),
      safe(() => apiFetch('/api/parametrizacoes')),
      safe(() => apiFetch('/api/historicos'))
    ])
    ativos.value = r1?.data ?? []
    exchanges.value = r2?.data ?? []
    redes.value = r3?.data ?? []
    parametrizacoes.value = r4?.data ?? []
    historicos.value = r5?.data ?? []
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.dashboard-container {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(190px, 1fr));
  gap: 1rem;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.25rem 1.5rem;
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px -4px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 46px;
  height: 46px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
}

.stat-label {
  font-size: 0.8rem;
  color: #64748b;
  font-weight: 500;
  line-height: 1;
}

.stat-value {
  font-size: 1.75rem;
  font-weight: 700;
  color: #0f172a;
  line-height: 1;
}

.skeleton-value {
  display: inline-block;
  width: 40px;
  height: 1.75rem;
  background: linear-gradient(90deg, #f1f5f9 25%, #e2e8f0 50%, #f1f5f9 75%);
  background-size: 200% 100%;
  animation: shimmer 1.2s infinite;
  border-radius: 6px;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.glass-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03);
  border: 1px solid #f1f5f9;
}

.last-check-banner {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  padding: 1.25rem 1.5rem;
  background: linear-gradient(135deg, #eff6ff 0%, #f0fdf4 100%);
  border: 1px solid #bfdbfe;
}

.last-check-icon {
  width: 42px;
  height: 42px;
  background: linear-gradient(135deg, #3b82f6, #10b981);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.last-check-text {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
}

.last-check-title {
  font-size: 0.8rem;
  font-weight: 600;
  color: #3b82f6;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.last-check-value {
  font-size: 1.05rem;
  font-weight: 700;
  color: #0f172a;
}

.last-check-value.no-data {
  color: #94a3b8;
  font-weight: 400;
}

.last-check-detail {
  font-size: 0.85rem;
  color: #475569;
}

.section {
  padding: 1.5rem;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.25rem;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.section-header h3 {
  margin: 0;
  font-size: 1rem;
  font-weight: 700;
  color: #0f172a;
}

.badge {
  background: #f1f5f9;
  color: #64748b;
  font-size: 0.75rem;
  font-weight: 600;
  padding: 0.2rem 0.6rem;
  border-radius: 20px;
}

.table-responsive { overflow-x: auto; }

.modern-table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  text-align: left;
}

.modern-table th {
  background: #f8fafc;
  color: #64748b;
  font-weight: 600;
  padding: 0.875rem 1rem;
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  border-bottom: 2px solid #e2e8f0;
}

.modern-table td {
  padding: 0.875rem 1rem;
  color: #334155;
  border-bottom: 1px solid #f1f5f9;
  vertical-align: middle;
}

.modern-table tbody tr { transition: background-color 0.15s; }
.modern-table tbody tr:last-child td { border-bottom: none; }
.modern-table tbody tr:hover { background-color: #f8fafc; }

.id-badge {
  font-size: 0.8rem;
  font-weight: 700;
  background: #e2e8f0;
  color: #475569;
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
}

.token-badge {
  font-size: 0.78rem;
  font-weight: 700;
  padding: 0.2rem 0.55rem;
  border-radius: 4px;
}
.token-badge.buy  { background: #dcfce7; color: #15803d; }
.token-badge.sell { background: #fee2e2; color: #b91c1c; }

.pair-sep {
  margin: 0 0.2rem;
  color: #cbd5e1;
  font-weight: 600;
}

.exchange-name {
  font-size: 0.88rem;
  color: #475569;
  font-weight: 500;
}

.cotacao {
  font-family: 'Courier New', monospace;
  font-size: 0.88rem;
  font-weight: 700;
  color: #0f172a;
}

.datetime {
  font-size: 0.85rem;
  color: #64748b;
}

.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 3rem;
  color: #64748b;
  gap: 0.75rem;
}

.spinner {
  width: 22px;
  height: 22px;
  border: 2px solid #e2e8f0;
  border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem;
  gap: 0.75rem;
  color: #94a3b8;
  text-align: center;
}

.empty-state p {
  margin: 0;
  font-size: 0.9rem;
}

@media (max-width: 640px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .stat-value { font-size: 1.4rem; }

  .last-check-banner { flex-direction: column; }

  .table-responsive { overflow-x: visible; }

  .modern-table,
  .modern-table tbody,
  .modern-table tr,
  .modern-table td { display: block; width: 100%; }

  .modern-table thead { display: none; }

  .modern-table tr {
    margin-bottom: 0.75rem;
    border: 1px solid #e2e8f0;
    border-radius: 12px;
    background: white;
    box-shadow: 0 2px 4px rgba(0,0,0,0.04);
    overflow: hidden;
  }

  .modern-table td {
    text-align: right;
    padding: 0.75rem 1rem;
    border-bottom: 1px solid #f1f5f9;
    position: relative;
    padding-left: 50%;
    display: flex;
    justify-content: flex-end;
    align-items: center;
    gap: 0.3rem;
    box-sizing: border-box;
  }

  .modern-table td:last-child { border-bottom: none; }

  .modern-table td::before {
    content: attr(data-label);
    position: absolute;
    left: 1rem;
    top: 50%;
    transform: translateY(-50%);
    width: calc(50% - 1.5rem);
    text-align: left;
    font-weight: 600;
    color: #94a3b8;
    font-size: 0.75rem;
    text-transform: uppercase;
  }
}

@media (max-width: 400px) {
  .stats-grid { grid-template-columns: 1fr; }
}
</style>
