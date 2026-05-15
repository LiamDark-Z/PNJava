import java.util.ArrayList;
import java.util.HashMap;
public class PNGParser {
    //variables used for parsing
    ArrayList<String> output;
    ArrayList<Pixel> input;
    ArrayList<Integer> operationTerms;
    HashMap<String,Symbol> classTable;
    HashMap<String, Symbol> methodTable;
    HashMap<String,String> jackConv;
    String className;
    String tempName;
    int forLocs;
    int classInt;
    int methodInt;
    int index;
    int tempindex;
    int labelInd;
    public PNGParser(ArrayList<Pixel> inArrayList){
        output = new ArrayList<>();
        operationTerms = new ArrayList<>();
        classTable = new HashMap<>();
        methodTable = new HashMap<>();
        jackConv = new HashMap<>();
        labelInd = 0;
        classInt = 0;
        methodInt = 0;
        input = inArrayList;
        index = 0;
        tempindex = 0;
        forLocs = 0;
        tempName="";
        //creates list of operator terms
        populateOps();
        popJackConv();
    }
    //debugging method
    public void printallin(){
        for(Pixel line:input){
            System.out.println(line.getRed());
        }
    }
    //another debugging method
    public void printallout(){
        for(String line:output){
            System.out.println(line);
        }
    }
    //returns an identifier of format I_r#g#b# or the equivalent jackOS identifier (as in the reference sheet)
    public String getCurrIdent(){
        String str = "I_r"+input.get(index).getRed()+"g"+input.get(index).getGreen()+"b"+input.get(index).getBlue();
        if(jackConv.containsKey(str)){str = jackConv.get(str);}
        return str;
    }

    public String getIndIdent(int i){
        String str = "I_r"+input.get(i).getRed()+"g"+input.get(i).getGreen()+"b"+input.get(i).getBlue();
        if(jackConv.containsKey(str)){str = jackConv.get(str);}
        return str;
    }

    //actual initialization for parsing
    public void parseList(){
        //checks for 0 size file
        if(index<input.size()){
            tempindex=index;
            if (input.get(tempindex).eq(250,255,255)){
                //initial reading of class declaration
                index++;
                className = getCurrIdent();
                index++;
                index++;
                //add classVarDec section
                addClassVarDecs();
                //add subroutineDec section
                addSubRoutineDecs();
                index++;
            }
        }
    }

    public void addClassVarDecs(){
        //iteratively populates the class symbol table
        Pixel curr = input.get(index);
        String type;
        int numOfKind;
        String kind;
        while(curr.eq(230,255,255)||curr.eq(225,255,255)){
            if(curr.eq(230,255,255)){
                kind = "this";
            }else{
                kind = "static";
            }
            index++;
            if(input.get(index).eq(215,255,255)){
                type = "int";
            }else if(input.get(index).eq(210,255,255)){
                type = "char";
            } else if(input.get(index).eq(205,255,255)){
                type = "bool";
            }else{
                type = getCurrIdent();
            }
            index++;
            numOfKind=0;
            for(Symbol entry: classTable.values()){
                if(entry.getKind().equals(kind)){
                    numOfKind++;
                }
            }
            classTable.put(getCurrIdent(), new Symbol(getCurrIdent(), type, kind, ""+numOfKind));
            index++;
            tempindex = index;
            //secondary while loop for multiple variable declarations
            while (!input.get(tempindex).eq(150,255,255)) {
                index++;
                numOfKind++;
                //adds info to class symbol table
                classTable.put(getCurrIdent(), new Symbol(getCurrIdent(), type, kind, ""+numOfKind));
                index++;
                tempindex = index;
            }
        index++;
        curr = input.get(index);
        }

        
    }
    public void addSubRoutineDecs(){
        //iteratively creates subroutines
        Pixel curr = input.get(index);
        String type;
        String kind = "argument";
        Pixel funcType;
        String funcName;
        int numOfKind;
        //function Main.(functionName) (# of local variables)
        while(curr.eq(245,255,255)||curr.eq(235,255,255)||curr.eq(240,255,255)){
            funcType = curr;
            methodTable.clear();
            //adds This to symbol table if function is a method
            if(funcType.eq(235,255,255)){
                methodTable.put("this",new Symbol("this", className, "argument", "0"));
            }
            index++;
            index++;
            funcName = getCurrIdent();
            index++;
            index++;
            tempindex = index;
            //while loop for adding parameters
            while (!input.get(tempindex).eq(175,255,255)) {
                if(input.get(index).eq(215,255,255)){
                    type = "int";
                }else if(input.get(index).eq(210,255,255)){
                    type = "char";
                } else if(input.get(index).eq(205,255,255)){
                    type = "bool";
                }else{
                    type = getCurrIdent();
                }
                index++;
                numOfKind=0;
                for(Symbol entry: methodTable.values()){
                    if(entry.getKind().equals(kind)){
                        numOfKind++;
                    }
                }
                //populates method symbol table
                methodTable.put(getCurrIdent(),new Symbol(getCurrIdent(), type, kind, ""+numOfKind));                
                index++;
                if(input.get(index).eq(155,255,255)){
                    index++;
                }
                tempindex = index;
            }
            
            index++;
            //adds subroutine body section
            addSubroutineBody(funcName, funcType);
            curr = input.get(index);
        }       
    }

    public void addSubroutineBody(String funcName, Pixel funcType){
        index++;
        String type;
        int numOfKind = 0;
        int locs = 0;
        Pixel curr = input.get(index);
        //adds vardecs in subroutine body to symbol table
        while(curr.eq(220,255,255)){
            locs++;
            index++;
            if(input.get(index).eq(215,255,255)){
                type = "int";
            }else if(input.get(index).eq(210,255,255)){
                type = "char";
            } else if(input.get(index).eq(205,255,255)){
                type = "bool";
            }else{
                type = getCurrIdent();
            }
            index++;
            methodTable.put(getCurrIdent(),new Symbol(getCurrIdent(), type, "local", ""+numOfKind));
            numOfKind++;
            index++;
            tempindex = index;
            while (!input.get(tempindex).eq(150,255,255)){
                locs++;
                index++;
                methodTable.put(getCurrIdent(),new Symbol(getCurrIdent(), type, "local", ""+numOfKind));
                numOfKind++;
                index++;
                tempindex = index;
            }
        index++;
        curr = input.get(index);
        }
        //creates the function, method, or constructor
        output.add("function "+className+"."+funcName+" "+locs);
        int fields = 0;
        if(funcType.eq(245,255,255)){
            for(Symbol entry: classTable.values()){
                if(entry.getKind().equals("this")){
                    fields++;
                }
            }
            output.add("push constant "+fields);
            output.add("call Memory.alloc 1");
            output.add("pop pointer 0");
        }else if(funcType.eq(235,255,255)){
            output.add("push argument 0");
            output.add("pop pointer 0");
        }
        //adds statements for the subroutine body
        addStatements();
        for(String o:output){
            if(o.equals("function "+className+"."+funcName+" "+locs)){
                output.set(output.indexOf(o),"function "+className+"."+funcName+" "+(locs+forLocs));
            }
        }
        forLocs = 0;
        index++;

    }

    public void addStatements(){
        Pixel curr = input.get(index);
        String temp;
        boolean isArray = false;
        while(curr.eq(95,255,255)||curr.eq(90,255,255)||curr.eq(85,255,255)||curr.eq(75,255,255)||curr.eq(70,255,255)||curr.eq(65,255,255)|curr.getType().equals("identifier")){
            //decides which statement type to use
            switch(curr.getRed()){
                case 95:
                    index++;
                    tempName = getCurrIdent();
                    index++;
                    //special case used for arrays (the book and slideshow really defines these poorly)
                    if(input.get(index).eq(170,255,255)){
                        isArray = true;
                        index++;
                        addExpression();
                        if(methodTable.containsKey(tempName)){
                            output.add("push "+methodTable.get(tempName).getKind()+" "+methodTable.get(tempName).getNum());
                        }else if(classTable.containsKey(tempName)){
                            output.add("push "+classTable.get(tempName).getKind()+" "+classTable.get(tempName).getNum());
                        }else{
                            System.err.println("invalid variable");
                            System.exit(1);
                        }
                        index++;
                        output.add("add");
                        index++;
                        addExpression();
                        output.add("pop temp 0");
                        output.add("pop pointer 1");
                        output.add("push temp 0");
                        output.add("pop that 0");
                    //Case for not arrays
                    }else{
                        index++;
                        addExpression();
                        if(methodTable.containsKey(tempName)){
                            output.add("pop "+methodTable.get(tempName).getKind()+" "+methodTable.get(tempName).getNum());
                        }else if(classTable.containsKey(tempName)){
                            output.add("pop "+classTable.get(tempName).getKind()+" "+classTable.get(tempName).getNum());
                        }else{
                            System.err.println("invalid variable");
                            System.exit(1);
                        }
                    }
                    index++;
                    break;

                case 85:
                    //creates unique labels for if statement
                    String L1;
                    String L2;
                    index++;
                    index++;
                    addExpression();
                    output.add("not");
                    L2 = className+"_"+labelInd;
                    labelInd++;
                    L1 = className+"_"+labelInd;
                    labelInd++;
                    output.add("if-goto "+L1);
                    
                    index++;
                    index++;
                    addStatements();
                    output.add("goto "+ L2);
                    output.add("label "+L1);
                    index++;
                    if(input.get(index).eq(80,255,255)){
                        index++;
                        index++;
                        addStatements();
                        index++;
                    }
                    output.add("label "+L2);
                    break;

                case 75:
                    //creates unique labels for while statement
                    String L3 = className+"_"+labelInd;
                    labelInd++;
                    String L4 = className+"_"+labelInd;
                    labelInd++;
                    index++;
                    index++;
                    output.add("label "+L3);
                    addExpression();
                    output.add("not");
                    output.add("if-goto "+L4);
                    index++;
                    index++;
                    addStatements();
                    output.add("goto "+L3);
                    output.add("label "+L4);
                    index++;
                    break;
                    
                    //Custom For statements of syntax: for [new identifier](expression){statements}
                    //for loop runs for expression iterations, and allows the access of identifier during runtime.
                    //identifier automatically decreases
                case 65:
                    String L5 = className+"_"+labelInd;
                    labelInd++;
                    String L6 = className+"_"+labelInd;
                    labelInd++;
                    index++;
                    temp = getCurrIdent();
                    methodTable.put(getCurrIdent(),new Symbol(getCurrIdent(), "int", "local", ""+methodTable.size()));
                    forLocs++;
                    index++;
                    index++;
                    addExpression();
                    index++;
                    index++;
                    output.add("pop "+methodTable.get(temp).getKind()+" "+methodTable.get(temp).getNum());
                    output.add("label "+L5);
                    output.add("push "+methodTable.get(temp).getKind()+" "+methodTable.get(temp).getNum());
                    output.add("push constant 0");
                    output.add("eq");
                    output.add("if-goto "+ L6);
                    addStatements();
                    output.add("push "+methodTable.get(temp).getKind()+" "+methodTable.get(temp).getNum());
                    output.add("push constant 1");
                    output.add("sub");
                    output.add("pop "+methodTable.get(temp).getKind()+" "+methodTable.get(temp).getNum());
                    output.add("goto "+L5);
                    output.add("label "+L6);
                    break;
                    
                case 90:
                    //calls subroutine
                    index++;
                    addSubroutineCall();
                    index++;
                    output.add("pop temp 0");
                    break;

                case 70:
                    //returns, 0 if no value given
                    index++;
                    if(!input.get(index).eq(150,255,255)){
                        addExpression();
                    }else{
                        output.add("push constant 0");
                    }
                    output.add("return");
                    index++;
                    break;

                default:
                    //Identifier -> Unary op
                    output.add("push "+methodTable.get(getCurrIdent()).getKind()+" "+methodTable.get(getCurrIdent()).getNum());
                    if((input.get(index+1).eq(145,255,255))){
                        output.add("push constant 1");
                        output.add("add");
                    }else if (input.get(index+1).eq(140,255,255)) {
                        output.add("push constant 1");
                        output.add("sub");
                    }
                    output.add("pop "+methodTable.get(getCurrIdent()).getKind()+" "+methodTable.get(getCurrIdent()).getNum());
                    index++;
                    index++;
                    index++;

                break;

            }
            curr = input.get(index);
        }
    }

    //adds an expression wherever called and pushes to stack
    public void addExpression(){
        addTerm();
        Pixel curr = input.get(index);
        while(operationTerms.contains(curr.getRed())&(curr.getGreen()==255)&(curr.getBlue()==255)){
            index++;
            addTerm();
            switch(curr.getRed()){
                case 145:
                    output.add("add");
                break;
                case 140:
                    output.add("sub");
                break;
                case 120:
                    output.add("or");
                break;
                case 125:
                    output.add("and");
                break;
                case 135:
                    output.add("call Math.multiply 2");
                break;
                case 130:
                    output.add("call Math.divide 2");
                break;
                case 115:
                    output.add("lt");
                break;
                case 110:
                    output.add("gt");
                break;
                case 105:
                    output.add("eq");
                break;
                default:

                break; 
            }
            
            curr = input.get(index);
        }
    }

    //adds a term where called and pushes to stack
    public void addTerm(){
        switch (input.get(index).getType()){
            case "integerConstant":
                output.add("push constant "+input.get(index).getValue());
                index++;
                break;

            case "charConstant":
                ArrayList<Integer> str = new ArrayList<>();
                while(input.get(index+1).getType().equals("charConstant")){
                    str.add(input.get(index).getValue());
                    index++;
                }
                str.add(input.get(index).getValue());
                output.add("push constant "+str.size());
                output.add("call String.new 1");
                for(int v: str){
                    output.add("push constant "+v);
                    output.add("call String.appendChar 2");
                }
                index++;
                break;

            case "identifier":
                if(input.get(index+1).eq(170,255,255)){
                    String arrVar = getCurrIdent();
                    index++;
                    index++;
                    addExpression();
                    if(methodTable.containsKey(arrVar)){
                        output.add("push "+methodTable.get(arrVar).getKind()+" "+methodTable.get(arrVar).getNum());
                    }else if(classTable.containsKey(arrVar)){
                        output.add("push "+classTable.get(arrVar).getKind()+" "+classTable.get(arrVar).getNum());
                    }else{
                        System.err.println("invalid variable");
                        System.exit(1);
                    }
                    output.add("add");
                    output.add("pop pointer 1");
                    output.add("push that 0");
                    index++;
                } else if(input.get(index+1).eq(180,255,255)|input.get(index+1).eq(160,255,255)){
                    addSubroutineCall();
                }else{
                    if(methodTable.containsKey(getCurrIdent())){
                        output.add("push "+methodTable.get(getCurrIdent()).getKind()+" "+methodTable.get(getCurrIdent()).getNum());
                    }else if(classTable.containsKey(getCurrIdent())){
                        output.add("push "+classTable.get(getCurrIdent()).getKind()+" "+classTable.get(getCurrIdent()).getNum());
                    }else{
                        System.err.println("invalid2 variable");
                        System.exit(1);
                    }
                    index++;
                }
                break;
            
            case "keyword":
                    switch(input.get(index).getRed()){
                        case 200:
                            output.add("push constant 1");
                            output.add("neg");
                        break;
                        case 195:
                            output.add("push pointer 0");
                        break;
                        default:
                            output.add("push constant 0");
                        break;
                    }
                index++;
                break;

            case "symbol":
                if(input.get(index).eq(180,255,255)){
                    index++;
                    addExpression();
                    index++;
                }else if(input.get(index).eq(100,255,255)){
                    index++;
                    addTerm();
                    output.add("not");
                }else{
                    index++;
                    addTerm();
                    output.add("neg");

                }
                break;

            default:
                break;    

        }

    }

    //adds a subroutine-call where called
    public void addSubroutineCall(){
        int params;
        String obj;
        String method;
        index++;
        if(input.get(index).eq(180,255,255)){
            method = getIndIdent(index-1);
            output.add("push pointer 0");
            index++;
            params = addExpressionList();
            index++;
            output.add("call "+className+"."+method+" "+(params+1));
        }
        else{
            int tempParams=0;
            obj = getIndIdent(index-1);
            if(methodTable.containsKey(obj)){
                tempParams++;
                output.add("push "+methodTable.get(obj).getKind()+" "+methodTable.get(obj).getNum());
                obj = methodTable.get(obj).getType();
            }else if(classTable.containsKey(obj)){
                tempParams++;
                output.add("push "+classTable.get(obj).getKind()+" "+classTable.get(obj).getNum());
                obj = classTable.get(obj).getType();
            }else{
            }
            index++;
            method = getCurrIdent();
            index++;
            index++;
            params = (addExpressionList()+tempParams);
            index++;
            output.add("call "+obj+"."+method+" "+params);
        }
    }

    //adds parameter as expressionlist for subroutine-calls
    public int addExpressionList(){
        int retval = 0;
            if(!input.get(index).eq(175,255,255)){
                retval++;
                addExpression();
                Pixel curr = input.get(index);
                while(curr.eq(155,255,255)){
                    retval++;
                    index++;
                    addExpression();
                    input.get(index);
                    curr = input.get(index);
                }
            }
        return retval;
    }

    //populates operators for expressions
    public void populateOps(){
        operationTerms.add(145);
        operationTerms.add(140);
        operationTerms.add(135);
        operationTerms.add(130);
        operationTerms.add(125);
        operationTerms.add(120);
        operationTerms.add(115);
        operationTerms.add(110);
        operationTerms.add(105);
    }

    //List of jackOS identifier equivalents
    public void popJackConv(){
        jackConv.put("I_r255g0b1","Math");
        jackConv.put("I_r255g3b0","multiply");
        jackConv.put("I_r255g4b0","divide");
        jackConv.put("I_r255g5b0","min");
        jackConv.put("I_r255g6b0","max");
        jackConv.put("I_r255g7b0","sqrt");
        jackConv.put("I_r255g0b2","String");
        jackConv.put("I_r255g1b0","new");
        jackConv.put("I_r255g2b0","dispose");
        jackConv.put("I_r255g8b0","length");
        jackConv.put("I_r255g9b0","charAt");
        jackConv.put("I_r255g10b0","setCharAt");
        jackConv.put("I_r255g11b0","appendChar");
        jackConv.put("I_r255g12b0","eraseLastChar");
        jackConv.put("I_r255g13b0","intValue");
        jackConv.put("I_r255g14b0","setInt");
        jackConv.put("I_r255g15b0","backSpace");
        jackConv.put("I_r255g16b0","newLine");
        jackConv.put("I_r255g0b3","Array");
        jackConv.put("I_r255g0b4","Output");
        jackConv.put("I_r255g17b0","moveCursor");
        jackConv.put("I_r255g18b0","printChar");
        jackConv.put("I_r255g19b0","printString");
        jackConv.put("I_r255g20b0","printInt");
        jackConv.put("I_r255g21b0","println");
        jackConv.put("I_r255g0b5","Screen");
        jackConv.put("I_r255g22b0","clearScreen");
        jackConv.put("I_r255g23b0","setColor");
        jackConv.put("I_r255g24b0","drawPixel");
        jackConv.put("I_r255g25b0","drawLine");
        jackConv.put("I_r255g26b0","drawRectagle");
        jackConv.put("I_r255g27b0","drawCircle");
        jackConv.put("I_r255g0b6","Keyboard");
        jackConv.put("I_r255g28b0","keyPressed");
        jackConv.put("I_r255g29b0","readChar");
        jackConv.put("I_r255g30b0","readLine");
        jackConv.put("I_r255g31b0","readInt");
        jackConv.put("I_r255g0b7","Memory");
        jackConv.put("I_r255g32b0","peek");
        jackConv.put("I_r255g33b0","poke");
        jackConv.put("I_r255g34b0","alloc");
        jackConv.put("I_r255g35b0","deAlloc");
        jackConv.put("I_r255g0b8","Sys");
        jackConv.put("I_r255g36b0","halt");
        jackConv.put("I_r255g37b0","error");
        jackConv.put("I_r255g38b0","wait");
        jackConv.put("I_r255g0b0","Main");
        jackConv.put("I_r255g100b100","main");
    }

    //returns list to main function
    public ArrayList<String> getList(){
        parseList();
        return output;
    }
}
