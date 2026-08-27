-- 비밀번호는 전부 password1234! 이고, BCrypt 해시로 저장되어 있습니다.
INSERT IGNORE INTO member (id, email, password, nickname, created_at, updated_at) VALUES
(1, 'moa@example.com',   '$2a$10$m1YEoJialw2FwBHUpl62wuKWEBoXLFQr5LisB9X03X41o56Ur86oy', '모아', NOW(), NOW()),
(2, 'sujin@example.com', '$2a$10$/ivi9dThquZz.UmxfX50DuE1Ib7bDRO5LLNPGgW6lF9lrceHegbNq', '수진', NOW(), NOW()),
(3, 'daeun@example.com', '$2a$10$pYTlG0L1RguOLCxoXWZwPuisdONSwXR8Q6e5yKvW4KcP6RXD8tbCO', '다은', NOW(), NOW()),
-- 비밀번호 temp1234 입니다.
(4, 'sparta@example.com', '$2a$10$BlSB3Of4B/aWSseCwSzNH.bzGtCMMgnG0m0sh0xdUwijQl9JB9Lka', '스파르타', NOW(), NOW());

-- 페이징이 바로 보이도록 글을 15개 넣어 둡니다.
-- 10개가 넘어야 두 번째 페이지가 생기고, 화면 아래에 페이지 버튼이 나타납니다.
-- created_at 을 한 시간씩 벌려 두었으므로 최신순 정렬이 눈에 보입니다.
INSERT IGNORE INTO post (id, member_id, title, content, created_at, updated_at) VALUES
(1, 1, '모아 게시판을 열었습니다', '앞으로 여기에 기능을 하나씩 붙여 나갑니다.', NOW() - INTERVAL 14 HOUR, NOW() - INTERVAL 14 HOUR),
(2, 2, '오늘 배운 것 정리', 'Controller 는 통역, Service 는 판단, Repository 는 저장.', NOW() - INTERVAL 13 HOUR, NOW() - INTERVAL 13 HOUR),
(3, 3, 'DTO 를 왜 쓰는지 이제 알겠어요', 'Entity 를 그대로 내보내면 비밀번호까지 나간다는 게 충격이었습니다.', NOW() - INTERVAL 12 HOUR, NOW() - INTERVAL 12 HOUR),
(4, 1, '3 Layer 구조가 아직 헷갈립니다', '어디까지가 Service 의 일인지 기준이 잘 안 잡히네요.', NOW() - INTERVAL 11 HOUR, NOW() - INTERVAL 11 HOUR),
(5, 2, '변경 감지 정말 신기합니다', 'save() 를 지웠는데도 UPDATE 가 나가는 걸 로그로 봤습니다.', NOW() - INTERVAL 10 HOUR, NOW() - INTERVAL 10 HOUR),
(6, 3, '도커로 MySQL 띄우기 성공', 'docker compose up -d 한 줄이면 끝이라 편하네요.', NOW() - INTERVAL 9 HOUR, NOW() - INTERVAL 9 HOUR),
(7, 1, 'SQL 로그 보는 법', 'application.yaml 에서 org.hibernate.SQL 을 debug 로 두면 됩니다.', NOW() - INTERVAL 8 HOUR, NOW() - INTERVAL 8 HOUR),
(8, 2, '페이징 기본값은 어디서 정하나요', 'Service 가 아니라 Controller 에서 정한다고 배웠습니다.', NOW() - INTERVAL 7 HOUR, NOW() - INTERVAL 7 HOUR),
(9, 3, 'Entity 에 Setter 를 안 두는 이유', '값이 언제 달라졌는지 추적할 수 없게 되기 때문이라고 합니다.', NOW() - INTERVAL 6 HOUR, NOW() - INTERVAL 6 HOUR),
(10, 1, '스터디 같이 하실 분', '주 2회로 생각하고 있습니다. 댓글 남겨 주세요.', NOW() - INTERVAL 5 HOUR, NOW() - INTERVAL 5 HOUR),
(11, 2, 'IntelliJ 단축키 정리해 봤습니다', '전체 검색은 Shift 두 번, TODO 목록은 Command 6 입니다.', NOW() - INTERVAL 4 HOUR, NOW() - INTERVAL 4 HOUR),
(12, 3, '커밋 메시지 규칙', 'feat, fix, refactor, test, chore 다섯 개만 기억하면 됩니다.', NOW() - INTERVAL 3 HOUR, NOW() - INTERVAL 3 HOUR),
(13, 1, '오늘 실습 후기', 'TODO 를 하나씩 채울 때마다 화면이 살아나는 게 재미있었습니다.', NOW() - INTERVAL 2 HOUR, NOW() - INTERVAL 2 HOUR),
(14, 2, '질문 있습니다', '404 와 500 은 결국 누구 잘못이냐로 나누는 게 맞을까요?', NOW() - INTERVAL 1 HOUR, NOW() - INTERVAL 1 HOUR),
(15, 3, '다음 회차 예고를 봤습니다', '검증과 예외 처리를 한다고 하네요. 미리 찾아봐야겠습니다.', NOW(), NOW());
