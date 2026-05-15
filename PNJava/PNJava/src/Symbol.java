public class Symbol {
    String name,type,kind,num;
    public Symbol(String nameIn, String typeIn, String kindIn, String numIn){
        name = nameIn;
        type = typeIn;
        kind = kindIn;
        num = numIn;
    }
    public String getName() {
        return name;
    }

    public String getKind() {
        return kind;
    }

    public String getType() {
        return type;
    }

    public String getNum() {
        return num;
    }

    public void print(){
        System.out.print(name+" ");
        System.out.print(kind+" ");
        System.out.print(type+" ");
        System.out.println(num);
    }
}
