#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Script to find Chinese text in Java, Vue, JS files and translate to English
Special rule: For words with 's' sound, write them out clearly without abbreviation
"""

import os
import re
import json
import time
import requests
from urllib.parse import quote
from pathlib import Path

class ChineseTranslator:
    def __init__(self):
        self.translations = {}
        self.cache_file = "code_translation_cache.json"
        self.load_cache()
        
    def load_cache(self):
        """Load existing translations from cache file"""
        try:
            with open(self.cache_file, 'r', encoding='utf-8') as f:
                self.translations = json.load(f)
            print(f"Loaded {len(self.translations)} translations from cache.")
        except FileNotFoundError:
            print("No translation cache found. Starting fresh.")
            self.translations = {}
    
    def save_cache(self):
        """Save translations to cache file"""
        with open(self.cache_file, 'w', encoding='utf-8') as f:
            json.dump(self.translations, f, ensure_ascii=False, indent=2)
    
    def translate_text(self, text, source_lang='zh', target_lang='en'):
        """
        Translate text using Google Translate API
        
        Args:
            text: Text to translate
            source_lang: Source language code (default: 'zh' for Chinese)
            target_lang: Target language code (default: 'en' for English)
        
        Returns:
            Translated text or original text if translation fails
        """
        if text in self.translations:
            return self.translations[text]
        
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
            
            # Apply special rule for words with 's' sound
            translated_text = self.apply_special_rules(text, translated_text)
            
            # Cache the translation
            self.translations[text] = translated_text
            self.save_cache()
            
            return translated_text
        
        except Exception as e:
            print(f"Error translating '{text}': {str(e)}")
            return text  # Return original text if translation fails
    
    def apply_special_rules(self, original_text, translated_text):
        """
        Apply special rules for translation
        Special rule: For words with 's' sound, write them out clearly without abbreviation
        """
        # Check if original text has 's' sound (simplified check for Chinese characters with 's' pinyin)
        s_sound_chars = ['s', 'x', 'sh', 'zh']
        
        # This is a simplified check - in a real implementation, you'd need pinyin conversion
        has_s_sound = any(char in original_text.lower() for char in s_sound_chars)
        
        if has_s_sound:
            # For words with 's' sound, ensure no abbreviations
            # Expand common abbreviations for personal pronouns with 's, 'm, 'll
            abbreviations = {
                # Personal pronouns with 's (is)
                "he's": "he is",
                "she's": "she is",
                "it's": "it is",
                "that's": "that is",
                "who's": "who is",
                "what's": "what is",
                "where's": "where is",
                "when's": "when is",
                "why's": "why is",
                "how's": "how is",
                "here's": "here is",
                "there's": "there is",
                
                # Personal pronouns with 'm (am)
                "I'm": "I am",
                
                # Personal pronouns with 'll (will)
                "I'll": "I will",
                "you'll": "you will",
                "he'll": "he will",
                "she'll": "she will",
                "it'll": "it will",
                "we'll": "we will",
                "they'll": "they will",
                "that'll": "that will",
                "who'll": "who will",
                "what'll": "what will",
                "where'll": "where will",
                "when'll": "when will",
                "why'll": "why will",
                "how'll": "how will",
                "here'll": "here will",
                "there'll": "there will",
                
                # Common contractions (non-personal pronouns)
                "let's": "let us",
                "don't": "do not",
                "won't": "will not",
                "can't": "cannot",
                "didn't": "did not",
                "doesn't": "does not",
                "haven't": "have not",
                "hasn't": "has not",
                "shouldn't": "should not",
                "wouldn't": "would not",
                "couldn't": "could not",
                "mustn't": "must not",
                "mightn't": "might not",
                "needn't": "need not",
                "isn't": "is not",
                "aren't": "are not",
                "wasn't": "was not",
                "weren't": "were not"
            }
            
            for abbrev, full_form in abbreviations.items():
                translated_text = translated_text.replace(abbrev, full_form)
        
        return translated_text
    
    def find_chinese_in_file(self, file_path):
        """
        Find all Chinese text in a file
        
        Args:
            file_path: Path to the file to scan
        
        Returns:
            List of tuples (line_number, chinese_text)
        """
        chinese_pattern = re.compile(r'[\u4e00-\u9fff]+')
        chinese_texts = []
        
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                lines = f.readlines()
                
            for line_num, line in enumerate(lines, 1):
                matches = chinese_pattern.findall(line)
                for match in matches:
                    chinese_texts.append((line_num, match))
                    
        except Exception as e:
            print(f"Error reading file {file_path}: {str(e)}")
            
        return chinese_texts
    
    def find_files(self, directory, extensions):
        """
        Find all files with specified extensions in directory and subdirectories
        
        Args:
            directory: Directory to search
            extensions: List of file extensions to include
        
        Returns:
            List of file paths
        """
        files = []
        for ext in extensions:
            files.extend(Path(directory).rglob(f"*.{ext}"))
        return files
    
    def translate_and_replace_in_file(self, file_path):
        """
        Find Chinese text in file, translate it, and replace with English
        
        Args:
            file_path: Path to the file to process
        
        Returns:
            Number of replacements made
        """
        chinese_texts = self.find_chinese_in_file(file_path)
        
        if not chinese_texts:
            return 0
            
        print(f"\nProcessing file: {file_path}")
        print(f"Found {len(chinese_texts)} Chinese text(s)")
        
        # Read the entire file
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
        except Exception as e:
            print(f"Error reading file {file_path}: {str(e)}")
            return 0
        
        # Apply translations
        modified_content = content
        replacement_count = 0
        
        for line_num, chinese_text in chinese_texts:
            if chinese_text in modified_content:
                # Translate the text
                english_translation = self.translate_text(chinese_text)
                
                # Replace in content
                modified_content = modified_content.replace(chinese_text, english_translation)
                replacement_count += 1
                
                print(f"  Line {line_num}: '{chinese_text}' -> '{english_translation}'")
        
        # Write the modified content back to the file
        try:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(modified_content)
            print(f"Updated file: {file_path} ({replacement_count} replacements)")
        except Exception as e:
            print(f"Error writing to file {file_path}: {str(e)}")
            return 0
            
        return replacement_count
    
    def process_directory(self, directory):
        """
        Process all Java, Vue, and JS files in directory and subdirectories
        
        Args:
            directory: Directory to process
        """
        # Find all relevant files
        # extensions = ['java', 'vue', 'js']
        extensions = ['sql']
        files = self.find_files(directory, extensions)
        
        print(f"Found {len(files)} files to process")
        
        total_replacements = 0
        processed_files = 0
        
        for file_path in files:
            replacements = self.translate_and_replace_in_file(file_path)
            if replacements > 0:
                total_replacements += replacements
                processed_files += 1
            
            # Add delay to avoid rate limiting
            time.sleep(0.5)
        
        print(f"\nTranslation completed!")
        print(f"Processed {processed_files} files with {total_replacements} total replacements")
        print(f"Translation cache saved to {self.cache_file}")

def main():
    """Main function to run the translation process"""
    print("Chinese to English Translation Tool for Code Files")
    print("=" * 50)
    
    # Get directory from user or use default
    directory = input("Enter directory path to search (default: current directory): ").strip()
    if not directory:
        directory = "."
    
    if not os.path.exists(directory):
        print(f"Error: Directory '{directory}' does not exist.")
        return
    
    # Create translator and process directory
    translator = ChineseTranslator()
    translator.process_directory(directory)

if __name__ == "__main__":
    main()