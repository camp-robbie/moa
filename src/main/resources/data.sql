-- 로그인이 아직 없어서 회원을 미리 넣어 둡니다.
-- 1번 회원이 PostService.TEMP_MEMBER_ID 입니다.
-- 비밀번호는 이후 수업에서 BCrypt 로 해싱합니다.
INSERT IGNORE INTO member (id, email, password, nickname, created_at, updated_at) VALUES
    (1, 'moa@example.com',   'password1234!', '모아', NOW(), NOW()),
    (2, 'sujin@example.com', 'password1234!', '수진', NOW(), NOW()),
    (3, 'daeun@example.com', 'password1234!', '다은', NOW(), NOW());

-- 목록 조회 구현 후 바로 보이도록 글도 몇 개 넣어 둡니다.
INSERT IGNORE INTO post (id, member_id, title, content, created_at, updated_at) VALUES
    (1, 1, '모아 게시판을 열었습니다',
        '앞으로 여기에 기능을 하나씩 붙여 나갑니다.', NOW(), NOW()),
    (2, 2, '오늘 배운 것 정리',
        'Controller 는 통역, Service 는 판단, Repository 는 저장.', NOW(), NOW()),
    (3, 3, 'DTO 를 왜 쓰는지 이제 알겠어요',
        'Entity 를 그대로 내보내면 비밀번호까지 나간다는 게 충격이었습니다.', NOW(), NOW());
