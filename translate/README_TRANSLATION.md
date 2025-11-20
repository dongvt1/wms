# Hướng dẫn dịch tiếng Trung sang tiếng Việt

## Mô tả
Script này được tạo để dịch các từ tiếng Trung trong file `remaining_chinese_for_translation.txt` sang tiếng Việt và cập nhật vào file SQL `jeecgboot-mysql-5.7_final_translated.sql`.

## Các file được tạo
1. `translate_chinese_to_vietnamese.py` - Script Python chính để thực hiện dịch
2. `run_translation.sh` - Script shell để chạy trên Linux/Mac
3. `run_translation.bat` - Script batch để chạy trên Windows
4. `translation_cache.json` - File cache lưu trữ các bản dịch (tự động tạo khi chạy)

## Cách sử dụng

### Trên Linux/Mac
```bash
chmod +x run_translation.sh
./run_translation.sh
```

### Trên Windows
```cmd
run_translation.bat
```

### Hoặc chạy trực tiếp với Python
```bash
python3 translate_chinese_to_vietnamese.py
```

## Yêu cầu hệ thống
- Python 3.x
- Gói requests (sẽ tự động cài đặt nếu chưa có)
- Kết nối internet để gọi API dịch

## Cách hoạt động
1. Đọc file `remaining_chinese_for_translation.txt` để lấy danh sách các từ tiếng Trung cần dịch
2. Sử dụng Google Translate API để dịch từng từ sang tiếng Việt
3. Lưu kết quả dịch vào file cache `translation_cache.json` để tránh dịch lại
4. Đọc file SQL `jeecgboot-mysql-5.7_final_translated.sql`
5. Thay thế các từ tiếng Trung bằng bản dịch tiếng Việt tương ứng
6. Lưu file SQL đã cập nhật
7. Kiểm tra và báo cáo các từ tiếng Trung còn lại (nếu có)

## Lưu ý
- Script sẽ tự động thêm độ trễ giữa các lần gọi API để tránh bị giới hạn
- Nếu một từ không dịch được, script sẽ giữ nguyên từ gốc
- File cache sẽ được cập nhật sau mỗi lần dịch thành công để tránh mất tiến độ
- Script sẽ hiển thị tối đa 20 từ tiếng Trung còn lại để tránh hiển thị quá nhiều

## Xử lý sự cố
- Nếu gặp lỗi "requests not installed", hãy cài đặt gói requests: `pip install requests`
- Nếu gặp lỗi kết nối, hãy kiểm tra kết nối internet
- Nếu file SQL không tìm thấy, hãy đảm bảo đường dẫn file đúng: `jeecg-boot/db/jeecgboot-mysql-5.7_final_translated.sql`