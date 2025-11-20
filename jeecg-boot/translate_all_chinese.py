#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import re
import sys

def translate_sql_file(input_file, output_file):
    # Đọc file SQL
    with open(input_file, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Từ điển dịch thuật đầy đủ
    translation_dict = {
        # Các từ đã có
        '创建人': 'Created by',
        '创建日期': 'Create date',
        '更新人': 'Updated by', 
        '更新日期': 'Update date',
        '所属部门': 'Department',
        '所属公司': 'Company',
        '性别': 'Gender',
        '生日': 'Birthday',
        '头像': 'Avatar',
        '状态': 'Status',
        '邮箱': 'Email',
        '手机号': 'Phone',
        '工号': 'Employee ID',
        '职位': 'Position',
        '职级': 'Job Level',
        '入职时间': 'Join Date',
        '离职时间': 'Leave Date',
        '是否离职': 'Is Resigned',
        '离职原因': 'Resignation Reason',
        '离职类型': 'Resignation Type',
        '离职交接人': 'Resignation Handover',
        '离职交接时间': 'Handover Time',
        '离职备注': 'Resignation Remarks',
        '离职状态': 'Resignation Status',
        '离职申请时间': 'Resignation Application Time',
        '离职审批时间': 'Resignation Approval Time',
        '离职生效时间': 'Resignation Effective Time',
        '离职申请人': 'Resignation Applicant',
        '离职审批人': 'Resignation Approver',
        '离职生效人': 'Resignation Effective Person'
    }
    
    # Thay thế các từ tiếng Trung bằng tiếng Anh
    for chinese, english in translation_dict.items():
        content = content.replace(chinese, english)
    
    # Ghi file kết quả
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write(content)
    
    print(f"Đã dịch file {input_file} thành {output_file}")

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Cách sử dụng: python translate_all_chinese.py <input_file> <output_file>")
        sys.exit(1)
    
    input_file = sys.argv[1]
    output_file = sys.argv[2]
    
    translate_sql_file(input_file, output_file)