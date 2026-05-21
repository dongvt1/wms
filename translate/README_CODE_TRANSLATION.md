# Công cụ dịch tiếng Trung sang tiếng Anh trong file mã nguồn

## Mô tả
Công cụ này được tạo để tìm kiếm chữ Trung trong các file mã nguồn (Java, Vue, JS) và dịch sang tiếng Anh với quy tắc đặc biệt:
- Đối với các từ có âm 's', viết rõ ràng không viết tắt

## Các file được tạo
1. `translate_chinese_in_code.py` - Script Python chính để thực hiện dịch
2. `run_code_translation.sh` - Script shell để chạy trên Linux/Mac
3. `run_code_translation.bat` - Script batch để chạy trên Windows
4. `code_translation_cache.json` - File cache lưu trữ các bản dịch (tự động tạo khi chạy)

## Cách sử dụng

### Trên Linux/Mac
```bash
chmod +x run_code_translation.sh
./run_code_translation.sh [đường_dẫn_thư_mục]
```

### Trên Windows
```cmd
run_code_translation.bat [đường_dẫn_thư_mục]
```

### Hoặc chạy trực tiếp với Python
```bash
python3 translate_chinese_in_code.py
```

Nếu không cung cấp đường dẫn thư mục, công cụ sẽ tìm kiếm trong thư mục hiện tại.

## Yêu cầu hệ thống
- Python 3.x
- Gói requests (sẽ tự động cài đặt nếu chưa có)
- Kết nối internet để gọi API dịch

## Cách hoạt động
1. Tìm kiếm tất cả file có đuôi .java, .vue, .js trong thư mục và các thư mục con
2. Quét từng file để tìm chữ Trung (sử dụng regex Unicode)
3. Với mỗi chữ Trung tìm thấy:
   - Gọi Google Translate API để dịch sang tiếng Anh
   - Áp dụng quy tắc đặc biệt cho từ có âm 's'
   - Lưu vào cache để tránh dịch lại
4. Thay thế chữ Trung bằng bản dịch tiếng Anh trong file gốc
5. Lưu file đã cập nhật
6. Báo cáo tổng số file đã xử lý và số lần thay thế

## Quy tắc đặc biệt cho từ có âm 's'
Công cụ sẽ tự động mở rộng các từ viết tắt phổ biến trong tiếng Anh, đặc biệt là các đại từ nhân xưng:

### Đại từ nhân xưng với 's (is):
- he's → he is
- she's → she is
- it's → it is
- that's → that is
- who's → who is
- what's → what is
- where's → where is
- when's → when is
- why's → why is
- how's → how is
- here's → here is
- there's → there is

### Đại từ nhân xưng với 'm (am):
- I'm → I am

### Đại từ nhân xưng với 'll (will):
- I'll → I will
- you'll → you will
- he'll → he will
- she'll → she will
- it'll → it will
- we'll → we will
- they'll → they will
- that'll → that will
- who'll → who will
- what'll → what will
- where'll → where will
- when'll → when will
- why'll → why will
- how'll → how will
- here'll → here will
- there'll → there will

### Các từ viết tắt phổ biến khác:
- let's → let us
- don't → do not
- won't → will not
- can't → cannot
- didn't → did not
- doesn't → does not
- haven't → have not
- hasn't → has not
- shouldn't → should not
- wouldn't → would not
- couldn't → could not
- mustn't → must not
- mightn't → might not
- needn't → need not
- isn't → is not
- aren't → are not
- wasn't → was not
- weren't → were not

## Lưu ý
- Script sẽ tự động thêm độ trễ giữa các lần gọi API để tránh bị giới hạn
- Nếu một từ không dịch được, script sẽ giữ nguyên từ gốc
- File cache sẽ được cập nhật sau mỗi lần dịch thành công để tránh mất tiến độ
- Script sẽ hiển thị thông tin chi tiết về mỗi lần thay thế (số dòng, từ gốc, bản dịch)
- Các file gốc sẽ được ghi đè với nội dung đã dịch, hãy sao lưu trước khi chạy

## Xử lý sự cố
- Nếu gặp lỗi "requests not installed", hãy cài đặt gói requests: `pip install requests`
- Nếu gặp lỗi kết nối, hãy kiểm tra kết nối internet
- Nếu không tìm thấy file, hãy đảm bảo đường dẫn thư mục đúng
- Nếu cần phục hồi file gốc, hãy sử dụng hệ thống quản lý phiên bản (git) hoặc sao lưu trước khi chạy

## Ví dụ sử dụng
```bash
# Dịch trong thư mục hiện tại
./run_code_translation.sh

# Dịch trong thư mục cụ thể
./run_code_translation.sh /path/to/project

# Dịch trong thư mục jeecg-boot
./run_code_translation.sh jeecg-boot