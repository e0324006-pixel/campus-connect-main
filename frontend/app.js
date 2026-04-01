/* ================================================
   Campus Connect — Shared JS Utilities
   ================================================ */

const API = 'http://localhost:8080/api';

function getToken()  { return localStorage.getItem('cc_token'); }
function getUserId() { return localStorage.getItem('cc_userId'); }
function getRole()   { return localStorage.getItem('cc_role'); }
function getName()   { return localStorage.getItem('cc_name'); }

function requireAuth() {
  if (!getToken()) { window.location.href = 'index.html'; return false; }
  return true;
}

function logout() {
  fetch(`http://localhost:8080/api/logout`, { method: 'POST', headers: authHeaders() });
  localStorage.clear();
  window.location.href = 'index.html';
}

function authHeaders() {
  return { 'Content-Type': 'application/json', 'Authorization': `Bearer ${getToken()}` };
}

async function apiFetch(path, options = {}) {
  try {
    const res = await fetch('http://localhost:8080' + path, {
      ...options,
      headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${getToken()}`, ...(options.headers || {}) }
    });
    if (res.status === 401) { localStorage.clear(); window.location.href = 'index.html'; return null; }
    return res;
  } catch (e) {
    console.error('API error:', path, e);
    return null;
  }
}

function formatDate(ts) {
  if (!ts) return '—';
  return new Date(parseInt(ts)).toLocaleDateString('en-IN', { day:'2-digit', month:'short', year:'numeric' });
}

function timeAgo(ts) {
  const diff = Date.now() - parseInt(ts);
  const d = Math.floor(diff / 86400000);
  if (d === 0) return 'Today';
  if (d === 1) return 'Yesterday';
  if (d < 7) return `${d} days ago`;
  return formatDate(ts);
}

function formatCTC(min, max) {
  const fmt = n => n >= 100000 ? (n/100000).toFixed(1) + ' LPA' : '₹' + n.toLocaleString();
  if (!min && !max) return 'Not disclosed';
  if (min === max || !max) return fmt(min);
  return `${fmt(min)} – ${fmt(max)}`;
}

function statusBadge(status) {
  const map = {
    'APPLIED':              ['badge-applied',    '📋 Applied'],
    'SHORTLISTED':          ['badge-shortlisted','⭐ Shortlisted'],
    'INTERVIEW_SCHEDULED':  ['badge-interview',  '📅 Interview'],
    'SELECTED':             ['badge-selected',   '✅ Selected'],
    'REJECTED':             ['badge-rejected',   '❌ Rejected'],
    'OPEN':                 ['badge-open',       '🟢 Open'],
    'CLOSED':               ['badge-closed',     '🔴 Closed'],
    'FULL_TIME':            ['badge-full_time',  '💼 Full Time'],
    'INTERNSHIP':           ['badge-internship', '🎓 Internship'],
    'PART_TIME':            ['badge-full_time',  '⏰ Part Time'],
    'PENDING':              ['badge-pending',    '⏳ Pending'],
    'APPROVED':             ['badge-approved',   '✅ Approved'],
  };
  const [cls, label] = map[status] || ['badge-applied', status];
  return `<span class="badge ${cls}">${label}</span>`;
}

function companyInitial(name) {
  return name ? name.charAt(0).toUpperCase() : '?';
}

function skillTagsHtml(skills, studentSkills = []) {
  if (!skills || skills.length === 0) return '<span class="text-muted text-xs">No skills listed</span>';
  return skills.map(sk => {
    if (studentSkills.length === 0) return `<span class="skill-tag">${sk}</span>`;
    const matched = studentSkills.some(s => s.toLowerCase() === sk.toLowerCase());
    return `<span class="skill-tag ${matched ? 'matched' : 'missing'}">${sk}</span>`;
  }).join('');
}

function matchBar(score) {
  const cls = score >= 70 ? 'high' : score >= 40 ? 'mid' : 'low';
  return `<div class="match-bar-wrap">
    <div class="match-label"><span>Skill Match</span><span style="color:var(--${cls==='high'?'success':cls==='mid'?'accent':'danger'})">${score}%</span></div>
    <div class="match-bar"><div class="match-fill ${cls}" style="width:${score}%"></div></div>
  </div>`;
}

function showToast(msg, type = 'success') {
  const t = document.createElement('div');
  t.className = `alert alert-${type}`;
  t.style.cssText = 'position:fixed;bottom:24px;right:24px;z-index:9999;min-width:280px;box-shadow:var(--shadow-lg);';
  t.textContent = msg;
  document.body.appendChild(t);
  setTimeout(() => t.remove(), 3000);
}

function renderSidebar(role) {
  const navs = {
    STUDENT: [
      { icon: '🏠', label: 'Dashboard', page: 'dashboard.html' },
      { icon: '💼', label: 'Browse Jobs', page: 'jobs.html' },
      { icon: '📋', label: 'My Applications', page: 'applications.html' },
      { icon: '🤖', label: 'Career Guidance', page: 'guidance.html' },
      { icon: '📅', label: 'Interviews', page: 'interviews.html' },
      { icon: '👤', label: 'My Profile', page: 'profile.html' },
    ],
    RECRUITER: [
      { icon: '🏠', label: 'Dashboard', page: 'dashboard.html' },
      { icon: '📢', label: 'Post a Job', page: 'post-job.html' },
      { icon: '💼', label: 'My Job Listings', page: 'my-jobs.html' },
      { icon: '👥', label: 'Applicants', page: 'applicants.html' },
      { icon: '📅', label: 'Interviews', page: 'interviews.html' },
      { icon: '🏢', label: 'Company Profile', page: 'profile.html' },
    ],
    ADMIN: [
      { icon: '🏠', label: 'Dashboard', page: 'dashboard.html' },
      { icon: '👥', label: 'All Students', page: 'admin-students.html' },
      { icon: '🏢', label: 'Recruiters', page: 'admin-recruiters.html' },
      { icon: '💼', label: 'All Jobs', page: 'admin-jobs.html' },
      { icon: '📊', label: 'Statistics', page: 'admin-stats.html' },
      { icon: '📣', label: 'Broadcast', page: 'admin-broadcast.html' },
    ]
  };

  const items = navs[role] || [];
  const current = window.location.pathname.split('/').pop();
  return `<nav class="sidebar">
    <div class="nav-section">
      <div class="nav-section-label">Menu</div>
      ${items.map(it => `
        <button class="nav-item ${current === it.page ? 'active' : ''}"
          onclick="window.location.href='${it.page}'">
          <span class="nav-icon">${it.icon}</span>
          ${it.label}
        </button>`).join('')}
    </div>
    <div class="nav-section" style="margin-top:auto;padding-top:16px;border-top:1px solid var(--border);">
      <button class="nav-item" onclick="logout()">
        <span class="nav-icon">🚪</span> Sign Out
      </button>
    </div>
  </nav>`;
}

function renderHeader(name, role, unread = 0) {
  const roleColors = { STUDENT: 'var(--success)', RECRUITER: 'var(--warning)', ADMIN: 'var(--danger)' };
  return `<header class="app-header">
    <div class="header-brand">
      <div class="brand-icon">🎓</div>
      <span class="brand-name">Campus<span>Connect</span></span>
    </div>
    <div class="header-actions">
      <button class="notif-btn" onclick="toggleNotifPanel()" title="Notifications">
        🔔
        ${unread > 0 ? `<span class="notif-badge">${unread}</span>` : ''}
      </button>
      <div class="user-chip">
        <div class="user-avatar">${name ? name.charAt(0) : '?'}</div>
        <div class="user-info">
          <div class="user-name">${name || 'User'}</div>
          <div class="user-role" style="color:${roleColors[role] || 'var(--text-muted)'}">${role}</div>
        </div>
      </div>
    </div>
  </header>`;
}

function renderNotifPanel(notifs) {
  return `<div class="notification-panel" id="notifPanel">
    <div class="notif-panel-header">
      <span>🔔 Notifications</span>
      <button class="btn btn-ghost btn-sm" onclick="markAllRead()">Mark all read</button>
    </div>
    <div class="notif-list" id="notifList">
      ${notifs.length === 0
        ? '<div class="empty-state" style="padding:24px"><div class="empty-icon">🔔</div><p>No notifications</p></div>'
        : notifs.slice(0, 30).map(n => `
          <div class="notif-item ${n.isRead ? '' : 'unread'}" onclick="markNotifRead('${n.notificationId}', this)">
            <div style="display:flex;gap:8px;align-items:flex-start;">
              ${!n.isRead ? '<div class="notif-dot" style="margin-top:5px;"></div>' : '<div style="width:8px"></div>'}
              <div>
                <div class="notif-msg">${n.message}</div>
                <div class="notif-time">${timeAgo(n.timestamp)}</div>
              </div>
            </div>
          </div>`).join('')
      }
    </div>
  </div>`;
}

let notifPanelOpen = false;
function toggleNotifPanel() {
  notifPanelOpen = !notifPanelOpen;
  const panel = document.getElementById('notifPanel');
  if (panel) panel.classList.toggle('open', notifPanelOpen);
}

async function markNotifRead(id, el) {
  el.classList.remove('unread');
  await apiFetch('/api/notifications/read', {
    method: 'POST', body: JSON.stringify({ notificationId: id })
  });
}

async function markAllRead() {
  await apiFetch('/api/notifications/read', {
    method: 'POST', body: JSON.stringify({ userId: getUserId() })
  });
  document.querySelectorAll('.notif-item.unread').forEach(el => el.classList.remove('unread'));
  document.querySelectorAll('.notif-badge').forEach(el => el.remove());
}

function getJobTypeColor(type) {
  const map = { 'FULL_TIME': 'var(--primary)', 'INTERNSHIP': 'var(--accent)', 'PART_TIME': 'var(--info)' };
  return map[type] || 'var(--text-muted)';
}

// ===== PATCHED requireAuth — accepts optional role =====
const _origRequireAuth = requireAuth;
function requireAuth(requiredRole) {
  if (!getToken()) { window.location.href = 'index.html'; return false; }
  if (requiredRole && getRole() !== requiredRole) { window.location.href = 'dashboard.html'; return false; }
  return true;
}

function getUser() {
  return { userId: getUserId(), name: getName(), role: getRole(), token: getToken() };
}


