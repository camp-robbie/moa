const BASE = '';

/* ---------- 토큰 보관: 쿠키 저장소 ----------
   로그인 응답의 accessToken을 쿠키에 넣어 두고, 새로고침하면 다시 꺼내 쓴다.
   서버는 이 쿠키를 읽지 않는다 — 인증은 그대로 Authorization 헤더로 보낸다. */
const C_TOKEN  = 'moa_token';
const C_ME     = 'moa_me';
const C_MAXAGE = 60 * 60;                 // JWT 만료(1시간)와 맞춘다

function readCookie(name){
  const hit = document.cookie.split('; ').find(c => c.startsWith(name + '='));
  return hit ? decodeURIComponent(hit.slice(name.length + 1)) : null;
}
function writeCookie(name, value){
  document.cookie =
    `${name}=${encodeURIComponent(value)}; path=/; max-age=${C_MAXAGE}; SameSite=Lax`;
}
function eraseCookie(name){
  document.cookie = `${name}=; path=/; max-age=0; SameSite=Lax`;
}

let token = readCookie(C_TOKEN), me = null;
try { me = JSON.parse(readCookie(C_ME) || 'null'); } catch(e){ me = null; }
if (!token || !me) { token = null; me = null; }   // 쿠키가 반쪽이면 로그아웃 상태로 시작

/* 로그인·로그아웃·401 — 토큰이 바뀌는 곳은 전부 여기를 지난다 */
function setAuth(t, m){
  token = t; me = m;
  if (t){ writeCookie(C_TOKEN, t); writeCookie(C_ME, JSON.stringify(m)); }
  else  { eraseCookie(C_TOKEN);    eraseCookie(C_ME); }
}
class ApiError extends Error{ constructor(status,message){ super(message); this.status=status; } }

async function call(method, url, body){
  const res = await fetch(BASE + url, {
    method,
    headers:{ 'Content-Type':'application/json',
              ...(token && { Authorization:`Bearer ${token}` }) },
    body: body === undefined ? undefined : JSON.stringify(body)
  });
  const json = res.status === 204 ? null : await res.json().catch(()=>null);
  if (res.status === 401 && !url.startsWith('/api/members/login')) {
    // 서버가 "누구인지 모르겠다"고 하면 로그인 화면으로 보낸다.
    // 로그인 기능이 아직 없는 회차에는 401 자체가 나오지 않으므로 이 길로 오지 않는다.
    setAuth(null, null); paintChrome();
    toast('로그인이 필요합니다');
    go('#/login');
  }
  if (!res.ok) throw new ApiError(res.status, json?.message ?? '요청을 처리하지 못했습니다');
  return json?.data ?? json;
}

/* 실시간 알림·쪽지 — 서버에 /ws 가 생기면 붙고, 없으면 조용히 넘어간다 */
let stomp = null;
function connectRealtime(){
  if (!token || stomp || !window.StompJs) return;
  stomp = new StompJs.Client({
    brokerURL: (location.protocol==='https:'?'wss://':'ws://') + location.host + '/ws',
    connectHeaders: { Authorization:`Bearer ${token}` },
    reconnectDelay: 0,                       // 없는 엔드포인트에 계속 재시도하지 않는다
    onConnect: () => {
      stomp.subscribe('/user/queue/notifications', f => {
        const n = JSON.parse(f.body);
        pushNoti({ who:n.who ?? n.sender, text:n.text ?? n.message, postId:n.postId });
      });
      stomp.subscribe('/user/queue/messages', async f => {
        const m = JSON.parse(f.body);
        if (location.hash.startsWith('#/messages') && curRoom && curRoom.id === m.roomId){
          ROOMS = await call('GET','/api/chat/rooms'); paintRooms(); await loadMsgs();
        } else pushNoti({ who:m.sender, text:'쪽지를 보냈습니다' });
      });
    },
    onWebSocketError: dropRealtime,
    onStompError:     dropRealtime
  });
  stomp.activate();
}
function dropRealtime(){ try{ stomp?.deactivate(); }catch(e){} stomp = null; }
