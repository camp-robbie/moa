/* ============================================================
   app.js
   ============================================================ */
const $ = s => document.querySelector(s);
const view = () => $('#view');
const set = (sel,html) => { const el=$(sel); if(el) el.innerHTML = html; };
const esc = s => String(s ?? '').replace(/[&<>"]/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;'}[c]));
const go = h => { location.hash = h; };

const PALETTE = ['#4f46e5','#0891b2','#db2777','#ea580c','#059669','#7c3aed','#0284c7','#b45309'];
function avatar(name, cls=''){
  const n = String(name||'?');
  let h = 0; for (const c of n) h = (h*31 + c.charCodeAt(0)) >>> 0;
  return `<span class="avatar ${cls}" style="background:${PALETTE[h%PALETTE.length]}">${esc(n.slice(0,1))}</span>`;
}
function ago(t){
  if (!t) return '';
  const d = new Date(String(t).replace(' ','T'));
  const s = (Date.now() - d.getTime())/1000;
  if (s < 60) return '방금';
  if (s < 3600) return `${Math.floor(s/60)}분 전`;
  if (s < 86400) return `${Math.floor(s/3600)}시간 전`;
  if (s < 86400*7) return `${Math.floor(s/86400)}일 전`;
  return `${d.getMonth()+1}월 ${d.getDate()}일`;
}
const hhmm = t => { const d=new Date(String(t).replace(' ','T'));
  const h=d.getHours(); return `${h<12?'오전':'오후'} ${((h+11)%12)+1}:${String(d.getMinutes()).padStart(2,'0')}`; };
const excerpt = s => String(s||'').replace(/\s+/g,' ').slice(0,140);

function toast(msg, onClick){
  const t = document.createElement('div');
  t.className = 'toast' + (onClick ? ' tap' : '');
  t.innerHTML = msg;
  if (onClick) t.onclick = () => { onClick(); t.remove(); };
  $('#toasts').appendChild(t);
  setTimeout(()=>t.remove(), 4600);
}
function fail(e){
  const box = $('#formErr');
  const msg = e instanceof ApiError ? e.message : '잠시 후 다시 시도해 주세요';
  if (box) box.textContent = msg; else toast(esc(msg));
}
function togglePop(id){
  const el = $('#'+id), was = !el.classList.contains('hidden');
  closePops(); if (!was) el.classList.remove('hidden');
}
function closePops(){ document.querySelectorAll('.pop').forEach(p=>p.classList.add('hidden')); }
document.addEventListener('click', e => { if (!e.target.closest('.rel')) closePops(); });

function paintChrome(){
  $('#btnLogin').classList.toggle('hidden', !!me);
  $('#meBtn').classList.toggle('hidden', !me);
  $('#bellBtn').classList.toggle('hidden', !me);
  $('#msgBtn').classList.toggle('hidden', !me);
  $('#tabTimeline').classList.toggle('hidden', !me);
  if (me) $('#meAvatar').innerHTML = avatar(me.nickname);
  const path = (location.hash||'#/posts').split('?')[0];
  $('#tabAll').classList.toggle('on', path.startsWith('#/posts'));
  $('#tabTimeline').classList.toggle('on', path === '#/timeline');
  paintNoti();
}

/* 사이드바 — 응답이 오는 것만 붙인다. 아직 없는 API는 그냥 빠진다 */
async function paintSide(){
  $('#side').innerHTML = me ? `
    <div class="card pad" style="display:flex;gap:13px;align-items:center">
      ${avatar(me.nickname,'lg')}
      <div style="min-width:0"><div style="font-weight:700">${esc(me.nickname)}</div>
      <div class="sub">${esc(me.email ?? '')}</div></div>
    </div>` : '';
  try{
    const r = await call('GET','/api/posts/ranking');
    if (!r?.length || !$('#side')) return;
    $('#side').insertAdjacentHTML('beforeend', `
      <div class="card">
        <div class="pad" style="padding-bottom:8px"><h2>실시간 인기글</h2></div>
        <div style="padding-bottom:12px">${r.map((x,i)=>`
          <div class="rank" onclick="go('#/posts/${x.id}')">
            <div class="rn">${i+1}</div><div class="rt">${esc(x.title)}</div></div>`).join('')}</div>
      </div>`);
  }catch(e){ /* 랭킹 API가 아직 없으면 위젯을 붙이지 않는다 */ }
}

/* ---------- 목록 ---------- */
const skeletonRows = () => Array(5).fill(`
  <div class="item" style="cursor:default">
    <div class="skel" style="width:32px;height:32px;border-radius:50%;flex:none"></div>
    <div style="flex:1">
      <div class="skel" style="width:58%;height:18px;margin-bottom:9px"></div>
      <div class="skel" style="width:92%;margin-bottom:6px"></div>
      <div class="skel" style="width:36%"></div>
    </div></div>`).join('');

async function viewList(){
  const q = new URLSearchParams(location.hash.split('?')[1] || '');
  const page = +(q.get('page')||0);
  const cond = { title:q.get('title')||'', nickname:q.get('nickname')||'',
    from:q.get('from')||'',   to:q.get('to')||'' };
  const searching = !!(cond.title || cond.nickname || cond.from || cond.to);

  view().innerHTML = `
    <div class="feedhead">
      <div><h1>${searching ? '검색 결과' : '전체글'}</h1><div class="sub" id="cnt">&nbsp;</div></div>
    </div>
    <div class="searchrow">
      <input id="q-title" placeholder="제목" value="${esc(cond.title)}"
             onkeydown="if(event.key==='Enter')doSearch()">
      <input id="q-nickname" placeholder="작성자" value="${esc(cond.nickname)}"
             onkeydown="if(event.key==='Enter')doSearch()">
      <input id="q-from" type="date" class="q-date" value="${esc(cond.from)}"
             onkeydown="if(event.key==='Enter')doSearch()">
      <span class="q-tilde">~</span>
      <input id="q-to" type="date" class="q-date" value="${esc(cond.to)}"
             onkeydown="if(event.key==='Enter')doSearch()">
      <button class="btn primary" onclick="doSearch()">검색</button>
    </div>
    ${searching ? `<div class="filters">
      ${chips(cond)}
      <button class="btn sm plain" onclick="go('#/posts')">전체 보기</button>
    </div>`:''}
    <div class="card"><div id="rows">${skeletonRows()}</div><div class="pager" id="pager"></div></div>`;
  paintSide();

  try{
    // 값이 있는 조건만 실어 보낸다. 빈 조건은 서버가 무시하지만 주소도 깔끔한 편이 낫다
    const sp = new URLSearchParams({ page, size:10 });
    for (const k of ['title','nickname','from','to']) if (cond[k]) sp.set(k, cond[k]);
    const url = searching
        ? `/api/posts/search?${sp}`
        : `/api/posts?page=${page}&size=10&sort=createdAt,desc`;
    const p = await call('GET', url);
    set('#cnt', p.totalElements ? `${p.totalElements.toLocaleString()}개의 글` : '&nbsp;');
    set('#rows', p.content.length ? p.content.map(post=>`
      <div class="item" onclick="go('#/posts/${post.id}')">
        ${avatar(post.nickname)}
        <div style="min-width:0;flex:1">
          <div class="it">${esc(post.title)}</div>
          <div class="ex">${esc(excerpt(post.content))}</div>
          <div class="meta">
            <b>${esc(post.nickname)}</b><span class="sep"></span><span>${ago(post.createdAt)}</span>
            ${post.likeCount ? `<span class="sep"></span><span>♥ ${post.likeCount}</span>`:''}
            ${post.commentCount ? `<span class="sep"></span><span>댓글 ${post.commentCount}</span>`:''}
          </div>
        </div></div>`).join('')
        : `<div class="empty"><div class="big">🔍</div>${searching?'조건에 맞는 글이 없습니다':'아직 글이 없습니다'}</div>`);
    drawPager(p, cond);
  }catch(e){
    set('#rows', `<div class="empty">${searching ? '검색을 사용할 수 없습니다' : esc(e.message)}</div>`);
    set('#pager','');
  }
}
function drawPager(p, cond){
  if (!$('#pager')) return;
  const total = Math.max(1, p.totalPages ?? 1), cur = p.number ?? 0;
  const start = Math.max(0, Math.min(cur-2, total-5)), end = Math.min(total, start+5);
  const qs = n => { const u = new URLSearchParams(); u.set('page',n);
    for (const k in cond) if (cond[k]) u.set(k,cond[k]); return `#/posts?${u}`; };
  let h = `<button ${cur===0?'disabled':''} onclick="go('${qs(cur-1)}')">‹</button>`;
  for (let i=start;i<end;i++) h += `<button class="${i===cur?'on':''}" onclick="go('${qs(i)}')">${i+1}</button>`;
  h += `<button ${cur>=total-1?'disabled':''} onclick="go('${qs(cur+1)}')">›</button>`;
  $('#pager').innerHTML = total > 1 ? h : '';
}
const CONDLABEL = { title:'제목', nickname:'작성자', from:'시작일', to:'종료일' };

function chips(cond){
  return Object.keys(CONDLABEL).filter(k => cond[k]).map(k => {
    const rest = new URLSearchParams();
    for (const j in cond) if (j !== k && cond[j]) rest.set(j, cond[j]);
    const href = rest.toString() ? '#/posts?' + rest : '#/posts';
    return `<span class="chip">${CONDLABEL[k]} ${esc(cond[k])}` +
        `<button title="이 조건만 빼기" onclick="go('${href}')">×</button></span>`;
  }).join('');
}

function doSearch(){
  const u = new URLSearchParams();
  const put = (k, el) => { const v = ($(el)?.value || '').trim(); if (v) u.set(k, v); };
  put('title',   '#q-title');
  put('nickname','#q-nickname');
  put('from',    '#q-from');
  put('to',      '#q-to');
  go(u.toString() ? '#/posts?' + u : '#/posts');
}

/* ---------- 상세 ---------- */
async function viewDetail(id){
  view().innerHTML = `<div class="card pad"><div class="skel" style="width:70%;height:28px;margin-bottom:22px"></div>
    <div class="skel" style="width:100%;margin-bottom:8px"></div><div class="skel" style="width:88%"></div></div>`;
  paintSide();
  let post;
  try{ post = await call('GET', `/api/posts/${id}`); }
  catch(e){
    view().innerHTML = `<div class="card"><div class="empty"><div class="big">🫥</div>${esc(e.message)}
      <div style="margin-top:16px"><button class="btn" onclick="go('#/posts')">전체글로</button></div></div></div>`;
    return;
  }
  // 로그인 기능이 아직 없으면(= 로그인하지 않은 상태) 내 글로 보고 버튼을 연다.
  // 서버에 소유권 검사가 생기면 그때부터 401·403이 돌아온다.
  const mine = !me || me.nickname === post.nickname;
  view().innerHTML = `
    <div class="card pad article">
      <h1>${esc(post.title)}</h1>
      <div class="byline">
        ${avatar(post.nickname,'lg')}
        <div style="flex:1;min-width:0">
          <div class="bname">${esc(post.nickname)}</div>
          <div class="meta"><span>${ago(post.createdAt)}</span>
            ${post.updatedAt && post.updatedAt!==post.createdAt?`<span class="sep"></span><span>수정됨</span>`:''}
            ${post.viewCount != null?`<span class="sep"></span><span>조회 ${post.viewCount}</span>`:''}</div>
        </div>
        ${me && !mine ? `<button class="btn sm"
          onclick="go('#/messages?to=${encodeURIComponent(post.nickname)}')">쪽지</button>`:''}
      </div>
      <div class="content">${esc(post.content)}</div>
      <div class="actions">
        ${post.likeCount != null ? `<button id="likeBtn" class="likebtn ${post.liked?'on':''}"
          onclick="toggleLike(${id},${!!post.liked})">♥ <span id="likeCnt">${post.likeCount}</span></button>`:''}
        <div class="grow"></div>
        ${mine?`<button class="btn sm" onclick="go('#/posts/${id}/edit')">수정</button>
                <button class="btn sm danger" onclick="removePost(${id})">삭제</button>`:''}
        <button class="btn sm plain" onclick="go('#/posts')">목록</button>
      </div>
    </div>
    <div class="card pad" id="cmCard" style="margin-top:16px">
      <div class="sectitle"><h2>댓글</h2><span class="sub" id="cmCnt"></span></div>
      <div id="comments"></div>
      <div id="moreWrap" style="text-align:center"></div>
      <div class="writer">
        ${avatar(me ? me.nickname : '?')}
        <div style="flex:1">
          <textarea id="cText" style="min-height:78px" placeholder="따뜻한 댓글을 남겨 주세요"></textarea>
          <div style="display:flex;align-items:center;gap:10px;margin-top:9px">
            <div class="formerr" id="formErr" style="flex:1;margin:0"></div>
            <button class="btn primary sm" onclick="addComment(${id})">등록</button>
          </div>
        </div>
      </div>
    </div>`;
  loadComments(id);
}

// 커서 페이징 : 댓글은 오래된 순(asc)이라 다음 장은 "더 최신" 이다.
// loaded 는 지금까지 화면에 그린 개수. 전체 개수를 서버가 세지 않으므로 우리가 누적한다.
async function loadComments(postId, cursor, loaded = 0){
  try{
    const list = await call('GET', `/api/posts/${postId}/comments?size=20${cursor?`&cursor=${cursor}`:''}`);
    const items = Array.isArray(list) ? list : list.content;
    const box = $('#comments'); if (!box) return;
    const html = items.length ? items.map(c=>{
      const mine = !!me && me.nickname === c.nickname;   // 로그아웃이면 버튼을 그리지 않는다
      return `<div class="cm" id="c${c.id}">
        ${avatar(c.nickname)}
        <div class="cmbody">
          <div class="cmtop"><span class="bname">${esc(c.nickname)}</span>
            <span class="sub">${ago(c.createdAt)}${c.updatedAt&&c.updatedAt!==c.createdAt?' · 수정됨':''}</span>
            ${mine?`<span class="cmacts">
              <button class="btn plain sm" onclick="editComment(${postId},${c.id})">수정</button>
              <button class="btn plain sm" onclick="removeComment(${postId},${c.id})">삭제</button></span>`:''}
          </div>
          <div class="cmtext">${esc(c.content)}</div>
        </div></div>`;
    }).join('') : (cursor ? '' : `<div class="empty" style="padding:36px 0">첫 댓글을 남겨 보세요</div>`);
    // 오래된 순이므로 다음 장은 아래에 이어 붙인다 (쪽지는 최신순이라 반대다)
    if (cursor) box.insertAdjacentHTML('beforeend', html); else box.innerHTML = html;
    const next  = Array.isArray(list) ? null : list.nextCursor;
    const total = loaded + items.length;

    // 커서 페이징은 전체 개수를 세지 않는다(count 쿼리를 안 쓰는 것이 장점).
    // 그래서 아직 남아 있으면 "20개+" 처럼 열어 두고, 다 불러오면 정확한 수가 된다.
    set('#cmCnt', total ? `${total}개${next ? '+' : ''}` : '');
    set('#moreWrap', next
        ? `<button class="btn sm plain" onclick="loadComments(${postId},'${next}',${total})">다음 댓글 더 보기</button>`
        : '');
  }catch(e){
    $('#cmCard')?.remove();     // 댓글 API가 아직 없으면 섹션째로 빠진다
  }
}
async function addComment(postId){
  const el = $('#cText'); if (!el.value.trim()) return;
  $('#formErr').textContent = '';
  try{ await call('POST', `/api/posts/${postId}/comments`, { content: el.value.trim() });
    el.value=''; loadComments(postId); }
  catch(e){ fail(e); }
}
async function editComment(postId,id){
  const content = prompt('댓글 수정', document.querySelector(`#c${id} .cmtext`).textContent);
  if (content === null) return;
  try{ await call('PUT', `/api/posts/${postId}/comments/${id}`, { content }); loadComments(postId); }
  catch(e){ toast(esc(e.message)); }
}
async function removeComment(postId,id){
  if (!confirm('댓글을 삭제할까요?')) return;
  try{ await call('DELETE', `/api/posts/${postId}/comments/${id}`); loadComments(postId); }
  catch(e){ toast(esc(e.message)); }
}
async function toggleLike(id, liked){
  try{
    const r = liked ? await call('DELETE',`/api/posts/${id}/likes`) : await call('POST',`/api/posts/${id}/likes`);
    $('#likeCnt').textContent = r?.likeCount ?? 0;
    $('#likeBtn').classList.toggle('on', !liked);
    $('#likeBtn').setAttribute('onclick', `toggleLike(${id},${!liked})`);
  }catch(e){ toast(esc(e.message)); }
}
async function removePost(id){
  if (!confirm('이 글을 삭제할까요?')) return;
  try{ await call('DELETE',`/api/posts/${id}`); toast('글을 삭제했습니다'); go('#/posts'); }
  catch(e){ toast(esc(e.message)); }
}

/* ---------- 작성 / 수정 ---------- */
async function viewForm(id){
  let post = { title:'', content:'' };
  if (id){ try{ post = await call('GET',`/api/posts/${id}`); }catch(e){ toast(esc(e.message)); } }
  view().innerHTML = `
    <div class="card pad narrowform">
      <h1 style="font-size:22px;margin-bottom:20px">${id?'글 수정':'새 글 쓰기'}</h1>
      <div class="field"><label>제목</label>
        <input id="fTitle" value="${esc(post.title)}" placeholder="제목을 입력하세요" maxlength="220">
        <div class="hint">200자까지 쓸 수 있습니다</div></div>
      <div class="field"><label>내용</label>
        <textarea id="fContent" style="min-height:330px" placeholder="무엇을 공유하고 싶으신가요?">${esc(post.content)}</textarea></div>
      <div class="formerr" id="formErr"></div>
      <div style="display:flex;gap:8px;justify-content:flex-end;margin-top:14px">
        <button class="btn" onclick="history.back()">취소</button>
        <button class="btn primary" onclick="submitPost(${id ?? 'null'})">${id?'수정하기':'등록하기'}</button>
      </div>
    </div>`;
  $('#side').innerHTML = '';
}
async function submitPost(id){
  $('#formErr').textContent = '';
  const body = { title:$('#fTitle').value, content:$('#fContent').value };
  try{
    const r = id ? await call('PUT',`/api/posts/${id}`,body) : await call('POST','/api/posts',body);
    toast(id?'수정했습니다':'글을 등록했습니다'); go(`#/posts/${r.id}`);
  }catch(e){ fail(e); }
}

/* ---------- 로그인 / 회원가입 ---------- */
function viewLogin(){
  view().innerHTML = `
    <div class="center"><div class="card pad">
      <h1 style="font-size:22px;text-align:center;margin-bottom:22px">로그인</h1>
      <div class="field"><label>이메일</label>
        <input id="lEmail" value="sparta@example.com" onkeydown="if(event.key==='Enter')login()"></div>
      <div class="field"><label>비밀번호</label>
        <input id="lPw" type="password" value="temp1234" onkeydown="if(event.key==='Enter')login()"></div>
      <div class="formerr" id="formErr"></div>
      <button class="btn primary" style="width:100%;margin-top:6px" onclick="login()">로그인</button>
      <div style="text-align:center;margin-top:16px;font-size:14px;color:var(--ink3)">
        아직 계정이 없으신가요?
        <button class="btn plain sm" style="color:var(--accent);font-weight:700" onclick="go('#/signup')">회원가입</button>
      </div>
    </div></div>`;
  $('#side').innerHTML = '';
}
async function login(){
  $('#formErr').textContent = '';
  try{
    const r = await call('POST','/api/members/login',{ email:$('#lEmail').value, password:$('#lPw').value });
    setAuth(r.accessToken, r.member ?? { nickname:'나' });
    paintChrome(); toast(`${esc(me.nickname)}님, 환영합니다`); go('#/posts');
    connectRealtime();
  }catch(e){ fail(e); }
}
function viewSignup(){
  view().innerHTML = `
    <div class="center"><div class="card pad">
      <h1 style="font-size:22px;text-align:center;margin-bottom:22px">회원가입</h1>
      <div class="field"><label>이메일</label><input id="sEmail" placeholder="you@example.com"></div>
      <div class="field"><label>비밀번호</label><input id="sPw" type="password" placeholder="8자 이상">
        <div class="hint">8자 이상 입력해 주세요</div></div>
      <div class="field"><label>닉네임</label><input id="sNick" placeholder="2~50자">
        <div class="hint">글과 댓글에 표시됩니다</div></div>
      <div class="formerr" id="formErr"></div>
      <button class="btn primary" style="width:100%;margin-top:6px" onclick="signup()">가입하기</button>
      <div style="text-align:center;margin-top:16px;font-size:14px;color:var(--ink3)">
        이미 계정이 있으신가요?
        <button class="btn plain sm" style="color:var(--accent);font-weight:700" onclick="go('#/login')">로그인</button>
      </div>
    </div></div>`;
  $('#side').innerHTML = '';
}
async function signup(){
  $('#formErr').textContent = '';
  try{
    await call('POST','/api/members/signup',
        { email:$('#sEmail').value, password:$('#sPw').value, nickname:$('#sNick').value });
    toast('가입이 완료되었습니다'); go('#/login');
  }catch(e){ fail(e); }
}
function logout(){ setAuth(null, null); NOTI=[]; dropRealtime(); closePops(); paintChrome(); toast('로그아웃했습니다'); go('#/posts'); }

/* ---------- 알림 ---------- */
let NOTI = [];
function paintNoti(){
  const un = NOTI.filter(n=>!n.read).length;
  const dot = $('#bellDot');
  if (dot){ dot.textContent = un > 9 ? '9+' : un; dot.classList.toggle('hidden', !un); }
  set('#notiList', NOTI.length ? NOTI.map(n=>`
    <button class="popitem ${n.read?'':'unread'}" onclick="openNoti(${n.id})">
      ${avatar(n.who,'sm')}
      <div style="flex:1;min-width:0">
        <div style="line-height:1.5;font-size:13.5px"><b>${esc(n.who)}</b>님이 ${esc(n.text)}</div>
        <div class="sub" style="font-size:12px">${ago(n.at)}</div>
      </div></button>`).join('')
      : `<div class="empty" style="padding:40px 0;font-size:14px">새 알림이 없습니다</div>`);
}
function pushNoti(n){
  NOTI.unshift({ id:Date.now()+Math.floor(Math.random()*1000), read:false,
    at:new Date().toISOString().slice(0,19), ...n });
  paintNoti();
  toast(`<span>🔔</span><span><b>${esc(n.who)}</b>님이 ${esc(n.text)}</span>`,
      () => { if (n.postId) go('#/posts/'+n.postId); });
}
function openNoti(id){
  const n = NOTI.find(x=>x.id===id); if(!n) return;
  n.read = true; closePops(); paintNoti();
  if (n.postId) go('#/posts/'+n.postId);
}
function readAllNoti(){ NOTI.forEach(n=>n.read=true); paintNoti(); }

/* ---------- 쪽지함 ---------- */
let ROOMS = [], curRoom = null;
async function viewMessages(){
  const to = new URLSearchParams(location.hash.split('?')[1]||'').get('to');
  view().innerHTML = `<div class="card msg"><div class="rooms" id="rooms"></div>
      <div class="thread" id="thread"></div></div>`;
  $('#side').innerHTML = '';
  try{
    ROOMS = await call('GET','/api/chat/rooms');
    if (to && !ROOMS.some(r=>r.partner===to)){
      const r = await call('POST','/api/chat/rooms',{ partner:to });
      ROOMS = await call('GET','/api/chat/rooms');
      curRoom = ROOMS.find(x=>x.id===r.id);
    } else curRoom = (to ? ROOMS.find(r=>r.partner===to) : ROOMS[0]) ?? null;
  }catch(e){
    view().innerHTML = `<div class="card"><div class="empty"><div class="big">💬</div>
      쪽지를 사용할 수 없습니다</div></div>`;
    return;
  }
  paintRooms();
  if (curRoom) openRoom(curRoom.id);
  else set('#thread', `<div class="empty"><div class="big">💬</div>주고받은 쪽지가 없습니다</div>`);
}
function paintRooms(){
  set('#rooms', ROOMS.length ? ROOMS.map(r=>`
    <div class="room ${curRoom&&r.id===curRoom.id?'on':''}" onclick="openRoom(${r.id})">
      ${avatar(r.partner)}
      <div style="flex:1;min-width:0">
        <div style="display:flex;gap:6px"><span class="bname" style="flex:1">${esc(r.partner)}</span>
          <span class="sub" style="font-size:11.5px">${ago(r.lastAt)}</span></div>
        <div class="sub" style="overflow:hidden;text-overflow:ellipsis;white-space:nowrap">${esc(r.lastMessage||'')}</div>
      </div></div>`).join('') : `<div class="empty" style="padding:50px 14px;font-size:14px">대화가 없습니다</div>`);
}
async function openRoom(id){
  curRoom = ROOMS.find(r=>r.id===id); if (!curRoom) return;
  paintRooms();
  set('#thread', `
    <div class="thead">${avatar(curRoom.partner)}<div class="bname">${esc(curRoom.partner)}</div></div>
    <div class="bubbles" id="bubbles"></div>
    <div class="composer">
      <input id="mText" placeholder="쪽지를 입력하세요" onkeydown="if(event.key==='Enter')sendMsg()">
      <button class="btn primary" onclick="sendMsg()">전송</button>
    </div>`);
  await loadMsgs();
}
async function loadMsgs(cursor){
  const r = await call('GET', `/api/chat/rooms/${curRoom.id}/messages?size=20${cursor?`&cursor=${cursor}`:''}`);
  const box = $('#bubbles'); if (!box) return;
  const html = r.content.map(m=>`
    <div class="brow ${m.mine?'me':'them'}">
      <div class="bub">${esc(m.content)}</div><div class="btime">${hhmm(m.sentAt)}</div>
    </div>`).join('');
  const more = r.nextCursor
      ? `<button class="btn sm plain" id="moreMsg" style="align-self:center;margin-bottom:10px"
         onclick="loadMsgs('${r.nextCursor}')">이전 대화 더 보기</button>` : '';
  if (cursor){
    const before = box.scrollHeight;
    $('#moreMsg')?.remove();
    box.insertAdjacentHTML('afterbegin', more + html);
    box.scrollTop = box.scrollHeight - before;
  } else {
    box.innerHTML = more + html;
    box.scrollTop = box.scrollHeight;
  }
}
async function sendMsg(){
  const el = $('#mText'), text = el.value.trim(); if (!text) return;
  el.value = '';
  await call('POST', `/api/chat/rooms/${curRoom.id}/messages`, { content:text });
  await loadMsgs();
}

/* ---------- 타임라인 ---------- */
async function viewTimeline(){
  view().innerHTML = `<div class="feedhead"><div><h1>팔로잉</h1>
      <div class="sub">팔로우한 사람의 글만 모아 봅니다</div></div></div>
    <div class="card"><div id="rows">${skeletonRows()}</div></div>`;
  paintSide();
  try{
    const p = await call('GET','/api/timeline?size=10');
    set('#rows', p.content.length ? p.content.map(post=>`
      <div class="item" onclick="go('#/posts/${post.id}')">
        ${avatar(post.nickname)}
        <div style="min-width:0;flex:1">
          <div class="it">${esc(post.title)}</div>
          <div class="ex">${esc(excerpt(post.content))}</div>
          <div class="meta"><b>${esc(post.nickname)}</b><span class="sep"></span><span>${ago(post.createdAt)}</span></div>
        </div></div>`).join('')
        : `<div class="empty">팔로우한 사람이 없습니다</div>`);
  }catch(e){
    set('#rows', `<div class="empty">타임라인을 사용할 수 없습니다</div>`);
  }
}

/* ---------- router ---------- */
const SOLO = ['#/login','#/signup','#/write','#/messages'];
function render(){
  const path = (location.hash||'#/posts').split('?')[0];
  document.querySelector('.wrap').classList.toggle('solo',
      SOLO.includes(path) || /^#\/posts\/\d+\/edit$/.test(path));
  closePops(); paintChrome();
  let m;
  if (path==='#/posts'||path==='#/'||path==='') return viewList();
  if (path==='#/write') return viewForm(null);
  if (path==='#/login') return viewLogin();
  if (path==='#/signup') return viewSignup();
  if (path==='#/messages') return me ? viewMessages() : go('#/login');
  if (path==='#/timeline') return me ? viewTimeline() : go('#/login');
  if ((m=path.match(/^#\/posts\/(\d+)\/edit$/))) return viewForm(+m[1]);
  if ((m=path.match(/^#\/posts\/(\d+)$/))) return viewDetail(+m[1]);
  view().innerHTML = `<div class="card"><div class="empty"><div class="big">🚧</div>준비 중입니다</div></div>`;
}
window.addEventListener('hashchange', render);

// 새로고침으로 들어왔는데 쿠키에 토큰이 남아 있으면 실시간도 다시 붙인다
if (token) connectRealtime();
render();
