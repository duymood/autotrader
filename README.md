# AutoTrader (Fabric 1.21.1)

Mod client tự động giao dịch **Sắt -> Ngọc Lục Bảo** khi mở GUI dân làng.

## Cách hoạt động
- Nhấn phím **X** trong game để bật/tắt tính năng (có thể đổi phím trong
  Options -> Controls -> Auto Trader).
- Khi bật, mỗi lần bạn click chuột phải vào một dân làng để mở GUI giao dịch,
  mod sẽ tự tìm ô "Sắt -> Ngọc Lục Bảo", giao dịch liên tục cho tới khi hết
  sắt hoặc dân làng hết lượt, rồi tự đóng GUI.
- Đây là mod **client-side** (chỉ cần cài ở máy bạn), không cần cài trên server
  Fabric nếu chơi multiplayer — nhưng nhiều server anti-cheat có thể phát hiện
  việc click liên tục bất thường, nên cân nhắc khi dùng trên server người khác.

## Cách build
Cần Java 21 (JDK) đã cài.

```
cd autotrader
./gradlew build
```

File mod (.jar) sẽ nằm trong `build/libs/autotrader-1.0.0.jar`
(chưa có gradlew wrapper sẵn trong gói này — chạy `gradle wrapper` một lần
nếu máy bạn đã có Gradle, hoặc tải template Fabric mới nhất và copy phần
`gradlew`, `gradlew.bat`, thư mục `gradle/` vào đây).

## Nếu build lỗi
Vì mình không có môi trường Minecraft/Fabric để build-test trực tiếp, một số
tên hàm/field (theo Yarn mappings) có thể lệch nhẹ giữa các bản build cụ thể
của 1.21.1. Nếu gặp lỗi "cannot find symbol", thường chỉ cần:
- Kiểm tra lại version chính xác trong `gradle.properties` tại
  https://fabricmc.net/develop/
- Đổi tên hàm bị báo lỗi trong `AutoTraderClient.java` sang tên tương ứng
  IntelliJ/VSCode gợi ý (autocomplete) — logic tổng thể không đổi.

## Cài đặt để chơi
1. Cài [Fabric Loader](https://fabricmc.net/use/) cho Minecraft 1.21.1.
2. Tải [Fabric API](https://modrinth.com/mod/fabric-api) đúng bản 1.21.1, bỏ vào thư mục `mods`.
3. Bỏ file `autotrader-1.0.0.jar` vừa build vào thư mục `mods`.
4. Khởi động Minecraft với profile Fabric.
