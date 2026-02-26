const whiteColor = '#ffffff'
const blackColor = '#666666'

export const Colors = [
  // Background color, text color
  ['#2196F3', whiteColor],
  ['#08C9C9', whiteColor],
  ['#00C345', whiteColor],
  ['#FAD714', whiteColor],
  ['#FF9300', whiteColor],
  ['#F52222', whiteColor],
  ['#EB2F96', whiteColor],
  ['#7500EA', whiteColor],
  ['#2D46C4', whiteColor],
  ['#484848', whiteColor],
  // --------------------
  ['#C9E6FC', blackColor],
  ['#C3F2F2', blackColor],
  ['#C2F1D2', blackColor],
  ['#FEF6C6', blackColor],
  ['#FFE5C2', blackColor],
  ['#FDCACA', blackColor],
  ['#FACDE6', blackColor],
  ['#DEC2FA', blackColor],
  ['#CCD2F1', blackColor],
  ['#D3D3D3', blackColor],
]

export const NONE_COLOR = ['#e9e9e9', blackColor]

/**
 * Returns a color iterator, returns a color on each call, starts over when colors are exhausted
 * @param {number} initIndex Initial color index
 * @returns {{getIndex: function, next: function}}
 */
export function getColorIterator(initIndex = 0) {
  let index = initIndex;
  if (index < 0 || index >= Colors.length) {
    index = 0;
  }
  return {
    getIndex: () => index,
    next() {
      const color = Colors[index];
      index = (index + 1) % Colors.length;
      return color;
    },
  }
}

/**
 * Get current coordinates and color based on color
 */
export function getItemColor(color) {
  if(!color){
    return NONE_COLOR[1];
  }
  let colorIndex = Colors.findIndex((value)=>{
    return value[0] === color;
  })
  if(colorIndex === -1){
    return NONE_COLOR[1];
  }
  return Colors[colorIndex][1];
}
