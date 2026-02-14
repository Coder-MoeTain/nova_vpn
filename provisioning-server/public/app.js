(function () {
  const peersBody = document.getElementById('peers-body');
  const peersTable = document.getElementById('peers-table');
  const loadingEl = document.getElementById('loading');
  const emptyEl = document.getElementById('empty');
  const errorEl = document.getElementById('error');
  const nextIpEl = document.getElementById('next-ip');
  const totalPeersEl = document.getElementById('total-peers');

  function showError(msg) {
    errorEl.textContent = msg;
    errorEl.hidden = false;
  }

  function hideError() {
    errorEl.hidden = true;
  }

  function formatDate(iso) {
    try {
      const d = new Date(iso);
      return d.toLocaleString();
    } catch (_) {
      return iso;
    }
  }

  function truncateKey(key) {
    if (!key || key.length < 12) return key;
    return key.slice(0, 8) + '…' + key.slice(-4);
  }

  function revoke(publicKey) {
    const btn = event && event.target;
    if (btn) btn.disabled = true;
    hideError();
    fetch('/api/peers', {
      method: 'DELETE',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ publicKey: publicKey }),
    })
      .then(function (res) {
        if (!res.ok) return res.json().then(function (b) { throw new Error(b.error || b.detail || res.status); });
        return load();
      })
      .catch(function (err) {
        showError(err.message || 'Failed to revoke peer');
        if (btn) btn.disabled = false;
      });
  }

  function load() {
    loadingEl.hidden = false;
    peersTable.hidden = true;
    emptyEl.hidden = true;
    hideError();

    fetch('/api/peers')
      .then(function (res) {
        if (!res.ok) throw new Error(res.status === 401 ? 'Authentication required' : 'Failed to load peers');
        return res.json();
      })
      .then(function (data) {
        loadingEl.hidden = true;
        nextIpEl.textContent = data.nextClientIp || '—';
        totalPeersEl.textContent = String(data.peers ? data.peers.length : 0);

        if (!data.peers || data.peers.length === 0) {
          emptyEl.hidden = false;
          return;
        }

        peersTable.hidden = false;
        peersBody.innerHTML = '';
        data.peers.forEach(function (p) {
          const tr = document.createElement('tr');
          tr.innerHTML =
            '<td class="key">' + truncateKey(p.publicKey) + '</td>' +
            '<td class="ip">' + (p.clientIp || '—') + '</td>' +
            '<td class="date">' + formatDate(p.createdAt) + '</td>' +
            '<td><button type="button" class="btn-revoke" data-key="' + p.publicKey.replace(/"/g, '&quot;') + '">Revoke</button></td>';
          tr.querySelector('.btn-revoke').addEventListener('click', function () {
            revoke(this.getAttribute('data-key'));
          });
          peersBody.appendChild(tr);
        });
      })
      .catch(function (err) {
        loadingEl.hidden = true;
        showError(err.message || 'Failed to load peers');
      });
  }

  load();
})();
