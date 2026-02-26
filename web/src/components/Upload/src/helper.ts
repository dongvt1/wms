export function checkFileType(file: File, accepts: string[]) {
  // update-begin--author:liaozhiyang---date:20250318---for：【issues/7954】BasicUploadComponent upload file，Limit upload format verification error
  const mimePatterns: string[] = [];
  const suffixList: string[] = [];
  // Classification processing accepts
  for (const item of accepts) {
    if (item.includes('/')) {
      mimePatterns.push(item);
    } else {
      // support.png or png（带点后缀or者不带点后缀）
      const suffix = item.startsWith('.') ? item.slice(1) : item;
      suffixList.push(suffix);
    }
  }
  // Suffix matching logic
  let suffixMatch = false;
  if (suffixList.length > 0) {
    const suffixRegex = new RegExp(`\\.(${suffixList.join('|')})$`, 'i');
    suffixMatch = suffixRegex.test(file.name);
  }
  // MIMEType matching logic
  let mimeMatch = false;
  if (mimePatterns.length > 0 && file.type) {
    mimeMatch = mimePatterns.some((pattern) => {
      // Escape special characters first，Reprocessing wildcards
      const regexPattern = pattern
        .replace(/[.+?^${}()|[\]\\]/g, '\\$&') // Escape special characters first
        .replace(/\*/g, '.*'); // Then replace the wildcard
      const regex = new RegExp(`^${regexPattern}$`, 'i');
      return regex.test(file.type);
    });
  }
  if (mimePatterns.length && suffixList.length) {
    return suffixMatch || mimeMatch;
  } else if (mimePatterns.length) {
    return mimeMatch;
  } else if (suffixList.length) {
    return suffixMatch;
  }
  // update-end--author:liaozhiyang---date:20250318---for：【issues/7954】BasicUploadComponent upload file，Limit upload format verification error
}

export function checkImgType(file: File) {
  return isImgTypeByName(file.name);
}

export function isImgTypeByName(name: string) {
  return /\.(jpg|jpeg|png|gif)$/i.test(name);
}

export function getBase64WithFile(file: File) {
  return new Promise<{
    result: string;
    file: File;
  }>((resolve, reject) => {
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = () => resolve({ result: reader.result as string, file });
    reader.onerror = (error) => reject(error);
  });
}
