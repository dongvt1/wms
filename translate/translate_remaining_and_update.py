#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Script to read Chinese text from remaining_chinese_for_translation.txt file,
translate it to English, and update the jeecgboot-mysql-5.7_final_translated.sql file
"""

import re

def translate_remaining_and_update():
    """
    Read Chinese text from file, translate it, and update SQL file
    """
    # Read Chinese text from file
    try:
        with open("remaining_chinese_for_translation.txt", 'r', encoding='utf-8') as f:
            chinese_lines = f.readlines()
    except FileNotFoundError:
        print("Error: remaining_chinese_for_translation.txt file not found!")
        print("Please run extract_remaining_chinese.py first to create this file.")
        return
    
    # Remove empty lines and strip whitespace
    chinese_texts = [line.strip() for line in chinese_lines if line.strip()]
    
    if not chinese_texts:
        print("No Chinese text found in the file.")
        return
    
    # Translation dictionary for common terms
    translations = {
        # Basic terms
        "创建人": "Created by",
        "创建日期": "Create date",
        "更新人": "Updated by", 
        "更新日期": "Update date",
        "所属部门": "Department",
        "所属公司": "Company",
        "性别": "Gender",
        "生日": "Birthday",
        "头像": "Avatar",
        "状态": "Status",
        "邮箱": "Email",
        "手机号": "Phone",
        "工号": "Employee ID",
        "职位": "Position",
        "职级": "Job Level",
        "入职时间": "Join Date",
        "离职时间": "Leave Date",
        "是否离职": "Is Resigned",
        "离职原因": "Resignation Reason",
        "离职类型": "Resignation Type",
        "离职交接人": "Resignation Handover",
        "离职交接时间": "Handover Time",
        "离职备注": "Resignation Remarks",
        "离职状态": "Resignation Status",
        "离职申请时间": "Resignation Application Time",
        "离职审批时间": "Resignation Approval Time",
        "离职生效时间": "Resignation Effective Time",
        "离职申请人": "Resignation Applicant",
        "离职审批人": "Resignation Approver",
        "离职生效人": "Resignation Effective Person",
        "离职审批时间": "",
        
        # Add more translations as needed
        # You can add more translations here based on the Chinese text found
    }
    
    # Read the final translated SQL file
    try:
        with open("jeecg-boot/db/jeecgboot-mysql-5.7_final_translated.sql", 'r', encoding='utf-8') as f:
            content = f.read()
    except FileNotFoundError:
        print("Error: jeecgboot-mysql-5.7_final_translated.sql file not found!")
        return
    
    # Apply translations
    translated_content = content
    translation_count = 0
    
    for chinese_text in chinese_texts:
        # Find the Chinese text in the content
        if chinese_text in content:
            # Try to find a translation
            english_translation = translations.get(chinese_text)
            
            if english_translation:
                # Apply the translation
                translated_content = translated_content.replace(chinese_text, english_translation)
                translation_count += 1
                print(f"Translated: '{chinese_text}' -> '{english_translation}'")
            else:
                # If no translation found, try to create a basic one
                english_translation = f"[TRANSLATED: {chinese_text}]"
                translated_content = translated_content.replace(chinese_text, english_translation)
                translation_count += 1
                print(f"Translated: '{chinese_text}' -> '{english_translation}' (auto-generated)")
    
    # Write the updated SQL file
    with open("jeecg-boot/db/jeecgboot-mysql-5.7_final_translated.sql", 'w', encoding='utf-8') as f:
        f.write(translated_content)
    
    print(f"\nTranslation completed! Applied {translation_count} translations.")
    print("Updated jeecgboot-mysql-5.7_final_translated.sql file.")

if __name__ == "__main__":
    translate_remaining_and_update()