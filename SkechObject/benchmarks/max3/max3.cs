int max3(int x, int y, int z){
    if(x>y) y = x;
    if(y>z) z = x; // z = y
    return z;
}
