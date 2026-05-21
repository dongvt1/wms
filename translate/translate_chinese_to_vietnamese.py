#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Script to translate Chinese text to Vietnamese using API and update SQL file
"""

import re
import json
import time
import requests
from urllib.parse import quote

def translate_text(text, source_lang='zh', target_lang='vi'):
    """
    Translate text using Google Translate API (free version)
    
    Args:
        text: Text to translate
        source_lang: Source language code (default: 'zh' for Chinese)
        target_lang: Target language code (default: 'vi' for Vietnamese)
    
    Returns:
        Translated text or original text if translation fails
    """
    try:
        # Using Google Translate API (free version)
        url = f"https://translate.googleapis.com/translate_a/single?client=gtx&sl={source_lang}&tl={target_lang}&dt=t&q={quote(text)}"
        
        headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36'
        }
        
        response = requests.get(url, headers=headers)
        response.raise_for_status()
        
        result = response.json()
        
        # Extract translated text from response
        translated_text = ''.join([item[0] for item in result[0] if item[0]])
        
        return translated_text
    
    except Exception as e:
        print(f"Error translating '{text}': {str(e)}")
        return text  # Return original text if translation fails

def translate_chinese_to_vietnamese():
    """
    Read Chinese text from file, translate it to Vietnamese, and update SQL file
    """
    # Read Chinese text from file
    try:
        with open("remaining_chinese_for_translation.txt", 'r', encoding='utf-8') as f:
            chinese_lines = f.readlines()
    except FileNotFoundError:
        print("Error: remaining_chinese_for_translation.txt file not found!")
        return
    
    # Remove empty lines and strip whitespace
    chinese_texts = [line.strip() for line in chinese_lines if line.strip()]
    
    if not chinese_texts:
        print("No Chinese text found in file.")
        return
    
    print(f"Found {len(chinese_texts)} Chinese texts to translate.")
    
    # Create translation dictionary
    translations = {}
    
    # Try to load existing translations from cache file
    try:
        with open("translation_cache.json", 'r', encoding='utf-8') as f:
            translations = json.load(f)
        print(f"Loaded {len(translations)} translations from cache.")
    except FileNotFoundError:
        print("No translation cache found. Starting fresh.")
    
    # Translate new texts
    new_translations = 0
    for i, chinese_text in enumerate(chinese_texts):
        if chinese_text not in translations:
            print(f"Translating {i+1}/{len(chinese_texts)}: '{chinese_text}'")
            vietnamese_translation = translate_text(chinese_text)
            translations[chinese_text] = vietnamese_translation
            new_translations += 1
            print(f"  -> '{vietnamese_translation}'")
            
            # Save cache after each translation to avoid losing progress
            with open("translation_cache.json", 'w', encoding='utf-8') as f:
                json.dump(translations, f, ensure_ascii=False, indent=2)
            
            # Add delay to avoid rate limiting
            time.sleep(0.5)
        else:
            print(f"Skipping {i+1}/{len(chinese_texts)}: '{chinese_text}' (already cached)")
    
    print(f"Added {new_translations} new translations to cache.")
    
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
        if chinese_text in content:
            vietnamese_translation = translations.get(chinese_text, chinese_text)
            translated_content = translated_content.replace(chinese_text, vietnamese_translation)
            translation_count += 1
            print(f"Applied translation: '{chinese_text}' -> '{vietnamese_translation}'")
    
    # Write updated SQL file
    with open("jeecg-boot/db/jeecgboot-mysql-5.7_final_translated.sql", 'w', encoding='utf-8') as f:
        f.write(translated_content)
    
    print(f"\nTranslation completed! Applied {translation_count} translations.")
    print("Updated jeecgboot-mysql-5.7_final_translated.sql file.")
    
    # Check for any remaining Chinese text
    chinese_pattern = re.compile(r'[\u4e00-\u9fff]+')
    remaining_chinese = chinese_pattern.findall(translated_content)
    
    if remaining_chinese:
        print(f"\nWarning: {len(set(remaining_chinese))} unique Chinese text strings still remain:")
        for text in sorted(set(remaining_chinese))[:20]:  # Show first 20
            print(f"  - {text}")
        if len(set(remaining_chinese)) > 20:
            print(f"  ... and {len(set(remaining_chinese)) - 20} more")
    else:
        print("\nSuccess! All Chinese text has been translated.")

if __name__ == "__main__":
    translate_chinese_to_vietnamese()