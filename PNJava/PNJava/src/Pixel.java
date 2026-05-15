

public class Pixel {
    int red, blue, green, value;
    String type;
    public Pixel(int r, int g, int b){
        red = r;
        green = g;
        blue = b;
        defType();
    }

    public void defType(){
        if((red==255)&(green==255)&(blue<255)){
            type = "charConstant";
            value = blue;
        }
        if((red==255)&(green<255)&(blue<255)){
            type = "identifier";
            value = 0;
        }
        if((red<255)&(green<255)&(blue<255)){
            type = "integerConstant";
            value = blue+(254*green)+(254*254*red);
        }
        if((((190<red)&(red<255))|(red<100))&(green==255)&(blue==255)){
            type = "keyword";
            value = 0;
        }
        if(((red<195)&(red>95))&(green==255)&(blue==255)){
            type = "symbol";
            value = 0;
        }
    }

    public boolean eq(int r,int g,int b){
        if((red==r)&(green==g)&(blue==b)){
            return true;
        }else{
            return false;
        }
    }

    public int getRed(){
        return red;
    }
    public int getGreen(){
        return green;
    }
    public int getBlue(){
        return blue;
    }
    public String getType(){
        return type;
    }
    public int getValue(){
        return value;
    }
}
