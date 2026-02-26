export const useCodeHinting = (CodeMirror, keywords, language) => {
  const currentKeywords: any = [...keywords];
  const codeHintingMount = (coder) => {
    if (keywords.length) {
      coder.setOption('mode', language);
      setTimeout(() => {
        coder!.on('cursorActivity', function () {
          coder?.showHint({
            completeSingle: false,
            // container: containerRef.value
          });
        });
      }, 1e3);
    }
  };

  const codeHintingRegistry = () => {
    // Custom keywords(.the previous level)
    const customKeywords: string[] = [];

    currentKeywords.forEach((item) => {
      if (item.superiors) {
        customKeywords.push(item.superiors);
      }
    });
    const funcsHint = (cm, callback) => {
      // Get cursor position
      const cur = cm.getCursor();
      // Get information about the current word
      const token = cm.getTokenAt(cur);
      const start = token.start;
      const end = cur.ch;
      const str = token.string;
      let recordKeyword = null;
      console.log('cursor position：', cur, 'word information：', token, `start:${start},end:${end},str:${str}`);

      if (str.length) {
        if (str === '.') {
          // Find.Are there any defined keywords before?
          const curLineCode = cm.getLine(cur.line);
          for (let i = 0, len = customKeywords.length; i < len; i++) {
            const k = curLineCode.slice(-(customKeywords[i].length + 1), -1);
            if (customKeywords.includes(k)) {
              recordKeyword = k;
              break;
            }
          }
        } else {
          // Find单词前面是否有.this(.keywords)
          const curLineCode = cm.getLine(cur.line);
          for (let i = 0, len = customKeywords.length; i < len; i++) {
            const k = curLineCode.slice(start - (customKeywords[i].length + 1), start);
            if (k.substr(-1) === '.' && customKeywords.includes(k.replace('.', ''))) {
              recordKeyword = k.replace('.', '');
              break;
            }
          }
        }
        const findIdx = (a, b) => a.toLowerCase().indexOf(b.toLowerCase());
        let list = currentKeywords.filter((item) => {
          if (recordKeyword) {
            // Check the properties of a specific objectormethod
            return item.superiors === recordKeyword;
          } else {
            // 查全局属性或者method
            return item.superiors == undefined;
          }
        });
        if (str === '.') {
          if (recordKeyword == null) {
            list = [];
          }
        } else {
          list = list
            .filter((item) => {
              const { text } = item;
              const index = findIdx(text, str);
              let result = text.startsWith('.') ? index === 1 : index === 0;
              return result;
            })
            .sort((a, b) => {
              if (findIdx(a.text, str) < findIdx(b.text, str)) {
                return -1;
              } else {
                return 1;
              }
            });
        }

        if (list.length === 1) {
          // When there is only one, it may be entered by yourself.，You need to remove the prompt when you lose to the end。
          const item = list[0];
          if (item.text === str || item.text.substring(1) === str) {
            list = [];
          }
        }
        if (list.length) {
          // whenstrRemove dots when they are not dots
          if (str != '.') {
            list = list.map((item) => {
              if (item.text.indexOf('.') === 0) {
                return { ...item, text: item.text.substring(1) };
              }
              return item;
            });
          }
          callback({
            list: list,
            from: CodeMirror.Pos(cur.line, start),
            to: CodeMirror.Pos(cur.line, end),
          });
          // update-begin--author:liaozhiyang---date:20240429---for：【QQYUN-8865】jsEnhanced and added mouse move prompts
          const item = currentKeywords[0];
          if (item?.desc) {
            setTimeout(() => {
              const elem: HTMLUListElement = document.querySelector('.CodeMirror-hints')!;
              if (elem) {
                const childElems = elem.children;
                Array.from(childElems).forEach((item) => {
                  const displayText = item.textContent;
                  const findItem = currentKeywords.find((item) => item.displayText === displayText);
                  if (findItem) {
                    item.setAttribute('title', findItem.desc);
                  }
                });
              }
            }, 0);
          }
          // update-end--author:liaozhiyang---date:20240429---for：【QQYUN-8865】jsEnhanced and added mouse move prompts
        } else {
        }
      }
    };
    funcsHint.async = true;
    funcsHint.supportsSelection = true;
    // autocomplete
    keywords.length && CodeMirror.registerHelper('hint', language, funcsHint);
  };
  return {
    codeHintingRegistry,
    codeHintingMount,
  };
};
