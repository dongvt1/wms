/*
 *
 * Fill in the user-defined expression here
 * available inOnlineUsed in form default value expressions
 * Variables or methods that need to be used externally must export，Otherwise it cannot be recognized
 * Example：
 *   export const name = 'Zhang San'; // const is a constant
 *   export let age = 17; // It depends on the situation export const still let ，Both work fine
 *   export function content(arg) { // export method，Passable parameters，Use parentheses，Must be worth itreturngo back，can returnPromise
 *     return 'content' + arg;
 *   }
 *   export const address = (arg) => content(arg) + ' | Beijing'; // export Arrow functions can also be used
 *
 */

/** 字段默认值官方Example：Get address */
export function demoFieldDefVal_getAddress(arg) {
  if (!arg) {
    arg = 'Chaoyang District';
  }
  return `Beijing ${arg}`;
}

/** CustomizeJS函数Example */
export function sayHi(name) {
  if (!name) {
    name = 'Zhang San';
  }
  return `Hello，my name is： ${name}`;
}
