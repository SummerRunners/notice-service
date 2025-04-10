// notice 데이터베이스 선택
db = db.getSiblingDB('notice');

// 사용자 생성
db.createUser({
    user: "srunners",    // 사용자 계정 이름
    pwd: "srunners",     // 사용자 계정 비밀번호
    roles: [
        { role: "readWrite", db: "notice" } // notice 데이터베이스에 읽기/쓰기 권한 부여
    ]
});

// 컬렉션 생성
db.createCollection("User");

// 초기 데이터 삽입
db.User.insertOne({ name: "이준영", age: 39, createdAt: new Date() });