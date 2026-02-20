// utils/Crypto/RandomStr.js
export function RandomStr($length) {
  let chars = 'ABCDFGHIJKLMNOPQRSTU1VWXYZabcdefghijklmnopqrstuvwxyz023456789E';
  let str = '';
  for (let i = 0; i < $length; i++) {
    str += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return str;
}
