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

  function escapeHtml(s) {
    if (s == null) return '';
    const div = document.createElement('div');
    div.textContent = s;
    return div.innerHTML;
  }

  let openDropdown = null;

  function closeAllDropdowns() {
    if (openDropdown) {
      openDropdown.classList.remove('dropdown-open');
      openDropdown = null;
    }
    document.removeEventListener('click', closeDropdownOnOutsideClick);
  }

  function closeDropdownOnOutsideClick(e) {
    if (openDropdown && !openDropdown.contains(e.target)) {
      closeAllDropdowns();
    }
  }

  function toggleActionDropdown(button, publicKey, deviceName) {
    const dropdown = button.nextElementSibling;
    if (openDropdown === dropdown) {
      closeAllDropdowns();
      return;
    }
    closeAllDropdowns();
    openDropdown = dropdown;
    dropdown.classList.add('dropdown-open');
    dropdown.dataset.publicKey = publicKey;
    dropdown.dataset.deviceName = deviceName;
    setTimeout(() => {
      document.addEventListener('click', closeDropdownOnOutsideClick);
    }, 0);
  }

  function openRenameModal(publicKey, currentName) {
    closeAllDropdowns();
    const modal = document.getElementById('modal-rename');
    if (!modal) return;
    document.getElementById('rename-peer-key').textContent = truncateKey(publicKey);
    document.getElementById('rename-input').value = currentName || '';
    document.getElementById('rename-input').dataset.publicKey = publicKey;
    modal.hidden = false;
    modal.style.display = 'flex';
    document.getElementById('rename-input').focus();
  }

  function closeRenameModal() {
    const modal = document.getElementById('modal-rename');
    if (modal) {
      modal.hidden = true;
      modal.style.display = 'none';
    }
  }

  function saveRename() {
    const input = document.getElementById('rename-input');
    const publicKey = input.dataset.publicKey;
    const deviceName = (input.value || '').trim();
    if (!publicKey) return;
    fetch('/api/peers', {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ publicKey: publicKey, deviceName: deviceName || null }),
    })
      .then(function (res) {
        if (!res.ok) return res.json().then(function (b) { throw new Error(b.error || b.detail || res.status); });
        closeRenameModal();
        load();
      })
      .catch(function (err) {
        showError(err.message || 'Failed to rename');
      });
  }

  function deletePeer(publicKey) {
    closeAllDropdowns();
    if (!confirm('Delete this peer from database? This will remove all data including location history, but the WireGuard peer will remain active.')) return;
    hideError();
    fetch('/api/peers/database-only', {
      method: 'DELETE',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ publicKey: publicKey }),
    })
      .then(function (res) {
        if (!res.ok) return res.json().then(function (b) { throw new Error(b.error || b.detail || res.status); });
        return load();
      })
      .catch(function (err) {
        showError(err.message || 'Failed to delete peer');
      });
  }

  function revoke(publicKey) {
    closeAllDropdowns();
    if (!confirm('Revoke this peer? This will remove it from WireGuard and the database.')) return;
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
      });
  }

  function openLocationHistoryModal(publicKey, deviceName) {
    closeAllDropdowns();
    const modal = document.getElementById('modal-location-history');
    if (!modal) return;
    document.getElementById('location-history-device-name').textContent = deviceName ? '(' + deviceName + ')' : '';
    document.getElementById('location-history-peer-key').textContent = truncateKey(publicKey);
    const listEl = document.getElementById('location-history-list');
    listEl.innerHTML = '<div class="loading">Loading…</div>';
    modal.hidden = false;
    modal.style.display = 'flex';
    fetch('/api/peers/' + encodeURIComponent(publicKey) + '/location-history?limit=100')
      .then(function (res) { return res.json(); })
      .then(function (data) {
        if (!data.locations || data.locations.length === 0) {
          listEl.innerHTML = '<p class="empty">No location history. Enable MySQL and use app to report location.</p>';
          return;
        }
        listEl.innerHTML = '';
        data.locations.forEach(function (loc) {
          const a = document.createElement('a');
          a.className = 'location-history-item';
          a.href = 'https://www.google.com/maps?q=' + encodeURIComponent(loc.latitude + ',' + loc.longitude);
          a.target = '_blank';
          a.rel = 'noopener';
          a.textContent = loc.latitude.toFixed(5) + ', ' + loc.longitude.toFixed(5) + ' — ' + formatDate(loc.createdAt);
          listEl.appendChild(a);
        });
      })
      .catch(function () {
        listEl.innerHTML = '<p class="error">Failed to load location history.</p>';
      });
  }

  function closeLocationHistoryModal() {
    const modal = document.getElementById('modal-location-history');
    if (modal) {
      modal.hidden = true;
      modal.style.display = 'none';
    }
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
          const statusClass = p.online ? 'status-online' : 'status-offline';
          const statusText = p.online ? 'Online' : 'Offline';
          const deviceName = p.deviceName || p.clientIp || '—';
          const deviceOverride = p.deviceNameOverride || null;
          const locationText = p.location || '—';
          const locationCell = (p.latitude != null && p.longitude != null)
            ? '<a class="location-link" href="https://www.google.com/maps?q=' + encodeURIComponent(p.latitude + ',' + p.longitude) + '" target="_blank" rel="noopener">' + escapeHtml(locationText) + '</a>'
            : escapeHtml(locationText);
          const traffic = (p.trafficRx != null && p.trafficTx != null) ? (p.trafficRx + ' / ' + p.trafficTx) : '—';
          const phone = p.phoneNumber || '—';
          const remoteIp = p.remoteIp || p.endpointIp || '—';
          tr.innerHTML =
            '<td class="status"><span class="status-dot ' + statusClass + '" title="' + statusText + '"></span> ' + statusText + '</td>' +
            '<td class="device">' + escapeHtml(deviceName) + '</td>' +
            '<td class="device-override-col">' + (deviceOverride ? escapeHtml(deviceOverride) : '—') + '</td>' +
            '<td class="phone">' + escapeHtml(phone) + '</td>' +
            '<td class="ip">' + (p.clientIp || '—') + '</td>' +
            '<td class="ip remote-ip">' + escapeHtml(remoteIp) + '</td>' +
            '<td class="location">' + locationCell + '</td>' +
            '<td class="traffic">' + escapeHtml(traffic) + '</td>' +
            '<td class="date">' + formatDate(p.createdAt) + '</td>' +
            '<td class="actions">' +
            '<div class="action-dropdown-container">' +
            '<button type="button" class="btn-action" data-key="' + p.publicKey.replace(/"/g, '&quot;') + '" data-name="' + escapeHtml(deviceName).replace(/"/g, '&quot;') + '">Action ▼</button>' +
            '<div class="action-dropdown">' +
            '<button type="button" class="dropdown-item" data-action="rename">Rename device</button>' +
            '<button type="button" class="dropdown-item" data-action="delete">Delete</button>' +
            '<button type="button" class="dropdown-item" data-action="history">Location history</button>' +
            '</div>' +
            '</div>' +
            '<button type="button" class="btn-revoke" data-key="' + p.publicKey.replace(/"/g, '&quot;') + '">Revoke</button>' +
            '</td>';
          const actionBtn = tr.querySelector('.btn-action');
          const dropdown = tr.querySelector('.action-dropdown');
          actionBtn.addEventListener('click', function (e) {
            e.stopPropagation();
            toggleActionDropdown(actionBtn, p.publicKey, deviceName);
          });
          dropdown.querySelector('[data-action="rename"]').addEventListener('click', function () {
            openRenameModal(p.publicKey, deviceName);
          });
          dropdown.querySelector('[data-action="delete"]').addEventListener('click', function () {
            deletePeer(p.publicKey);
          });
          dropdown.querySelector('[data-action="history"]').addEventListener('click', function () {
            openLocationHistoryModal(p.publicKey, deviceName);
          });
          const revokeBtn = tr.querySelector('.btn-revoke');
          revokeBtn.addEventListener('click', function () {
            revoke(p.publicKey);
          });
          peersBody.appendChild(tr);
        });
      })
      .catch(function (err) {
        loadingEl.hidden = true;
        showError(err.message || 'Failed to load peers');
      });
  }

  document.getElementById('rename-cancel').addEventListener('click', closeRenameModal);
  document.getElementById('rename-save').addEventListener('click', saveRename);
  document.getElementById('rename-input').addEventListener('keydown', function (e) {
    if (e.key === 'Enter') saveRename();
    if (e.key === 'Escape') closeRenameModal();
  });
  document.getElementById('modal-rename').addEventListener('click', function (e) {
    if (e.target === this) closeRenameModal();
  });
  document.getElementById('location-history-close').addEventListener('click', closeLocationHistoryModal);
  document.getElementById('location-history-close-x').addEventListener('click', closeLocationHistoryModal);
  document.getElementById('modal-location-history').addEventListener('click', function (e) {
    if (e.target === this) closeLocationHistoryModal();
  });
  document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape') {
      closeRenameModal();
      closeLocationHistoryModal();
    }
  });

  load();
  setInterval(load, 30000);
})();
