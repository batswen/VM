package com.batswen.vm;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;

public class VM {
    static Map<String, Mnemonic> str_to_opcodes = new HashMap<>();
    static String[] string_pool;
    static List<Byte> code = new ArrayList<>();
    static Stack<Integer> stack = new Stack<>();
    private static final int WORD_SIZE = 4;
    static enum Mnemonic {
        NONE, FETCH, STORE, PUSH, ADD, SUB, MUL, DIV, MOD, LT, GT, LE, GE, EQ, NE, AND, OR, NEG, NOT,
        JMP, JZ, PRTC, PRTS, PRTI, HALT
    }

    static void run(int datasize) {
        boolean running = true;
        int pc = 0, val;
        for (int i = 0; i < datasize; i++) {
            stack.push(0);
        }
        
        while (running) {
            switch (Mnemonic.values()[code.get(pc++)]) {
                case FETCH:
                    val = 0;
                    for (int i = 0; i < WORD_SIZE; i++) {
                        val = val * 256 + (code.get(pc + i) & 0xff);
                    }
                    stack.push(stack.get(val));
                    pc += WORD_SIZE;
                    break;
                case STORE:
                    val = 0;
                    for (int i = 0; i < WORD_SIZE; i++) {
                        val = val * 256 + (code.get(pc + i) & 0xff);
                    }
                    stack.set(val, stack.pop());
                    pc += WORD_SIZE;
                    break;
                case PUSH:
                    val = 0;
                    for (int i = 0; i < WORD_SIZE; i++) {
                        val = val * 256 + (code.get(pc + i) & 0xff);
                    }
                    stack.push(val);
                    pc += WORD_SIZE;
                    break;
                case ADD:
                    val = stack.pop();
                    stack.push(stack.pop() + val);
                    break;
                case SUB:
                    val = stack.pop();
                    stack.push(stack.pop() - val);
                    break;
                case MUL:
                    val = stack.pop();
                    stack.push(stack.pop() * val);
                    break;
                case DIV:
                    val = stack.pop();
                    stack.push(stack.pop() / val);
                    break;
                case MOD:
                    val = stack.pop();
                    stack.push(stack.pop() % val);
                    break;
                case LT:
                    val = stack.pop();
                    int val2 = stack.pop();
                    stack.push(val2 < val ? 1 : 0);
                    break;
                case GT:
                    val = stack.pop();
                    stack.push(stack.pop() > val ? 1 : 0);
                    break;
                case LE:
                    val = stack.pop();
                    stack.push(stack.pop() <= val ? 1 : 0);
                    break;
                case GE:
                    val = stack.pop();
                    stack.push(stack.pop() >= val ? 1 : 0);
                    break;
                case EQ:
                    val = stack.pop();
                    stack.push(stack.pop() == val ? 1 : 0);
                    break;
                case NE:
                    val = stack.pop();
                    stack.push(stack.pop() != val ? 1 : 0);
                    break;
                case AND:
                    val = stack.pop();
                    stack.push(stack.pop() & val);
                    break;
                case OR:
                    val = stack.pop();
                    stack.push(stack.pop() | val);
                    break;
                case NEG:
                    stack.push(-stack.pop());
                    break;
                case NOT:
                    stack.push(stack.pop() == 0 ? 1 : 0);
                    break;
                case JMP:
                    val = 0;
                    for (int i = 0; i < WORD_SIZE; i++) {
                        val = val * 256 + (code.get(pc + i) & 0xff);
                    }
                    pc = val;
                    break;
                case JZ:
                    val = stack.pop();
                    if (val == 0) {
                        for (int i = 0; i < WORD_SIZE; i++) {
                            val = val * 256 + (code.get(pc + i) & 0xff);
                        }
                        pc = val;
                    } else {
                        pc += WORD_SIZE;
                    }
                    break;
                case PRTC:
                    System.out.printf("%c", (char)(int)stack.pop());
                    break;
                case PRTS:
                    System.out.printf("%s", string_pool[stack.pop()]);
                    break;
                case PRTI:
                    System.out.printf("%d", stack.pop());
                    break;
                case HALT:
                    running = false;
                    break;
            }
        }
    }
    static byte[] int_to_bytes(int data) {
        byte[] result = new byte[4];

        result[0] = (byte) ((data & 0xFF000000) >> 24);
        result[1] = (byte) ((data & 0x00FF0000) >> 16);
        result[2] = (byte) ((data & 0x0000FF00) >> 8);
        result[3] = (byte) ((data & 0x000000FF) >> 0);

        return result;
    }
    static int bytes_to_int(byte[] b) {
        int result = 0;
        for (int i = 0; i < WORD_SIZE; i++) {
            result = result * 256 + b[i];
        }
        return result;
    }
    static String str(String s) {
        String result = "";
        int i = 0;
        s = s.replace("\"", "");
        while (i < s.length()) {
            if (s.charAt(i) == '\\' && i + 1 < s.length()) {
                if (s.charAt(i + 1) == 'n') {
                    result += '\n';
                    i += 2;
                } else if (s.charAt(i) == '\\') {
                    result += '\\';
                    i += 2;
                } 
            } else {
                result += s.charAt(i);
                i++;
            }
        }
        return result;
    }
    static int load_code(String filename) throws FileNotFoundException {
        Scanner s = new Scanner(new File(filename));
        String[] line_parts;
        int data_size;
        int offset;
        int num_strings;
        byte[] bytes;
        Mnemonic opcode;

        line_parts = s.nextLine().split(" ");
        
        data_size = Integer.parseInt(line_parts[1]);
        num_strings = Integer.parseInt(line_parts[3]);
        
        string_pool = new String[num_strings];
        for (int i = 0; i < num_strings; i++) {
            string_pool[i] = str(s.nextLine().replace("\"", ""));
        }
 
        while (s.hasNext()) {
            line_parts = s.nextLine().trim().split("\\s+");

            offset = Integer.parseInt(line_parts[0]);
            opcode = str_to_opcodes.get(line_parts[1]);
            code.add((byte)opcode.ordinal());
            switch (opcode) {
                case JMP:
                case JZ:
                    bytes = int_to_bytes(Integer.parseInt(line_parts[3].replaceAll("\\(|\\)","")));
                    for (int i = 0; i < WORD_SIZE; i++) {
                        code.add(bytes[i]);
                    }
                    break;
                case PUSH:
                    bytes = int_to_bytes(Integer.parseInt(line_parts[2]));
                    for (int i = 0; i < WORD_SIZE; i++) {
                        code.add(bytes[i]);
                    }
                    break;
                case FETCH:
                case STORE:
                    bytes = int_to_bytes(Integer.parseInt(line_parts[2].replaceAll("\\[|\\]","")));
                    for (int i = 0; i < WORD_SIZE; i++) {
                        code.add(bytes[i]);
                    }
                    break;
                default:
                    break;
            }
        }
        
        return data_size;
    }
    public static void main(String[] args) { 
        str_to_opcodes.put("fetch", Mnemonic.FETCH);
        str_to_opcodes.put("push", Mnemonic.PUSH);
        str_to_opcodes.put("store", Mnemonic.STORE);
        str_to_opcodes.put("add", Mnemonic.ADD);
        str_to_opcodes.put("sub", Mnemonic.SUB);
        str_to_opcodes.put("mul", Mnemonic.MUL);
        str_to_opcodes.put("div", Mnemonic.DIV);
        str_to_opcodes.put("mod", Mnemonic.MOD);
        str_to_opcodes.put("lt", Mnemonic.LT);
        str_to_opcodes.put("le", Mnemonic.LE);
        str_to_opcodes.put("gt", Mnemonic.GT);
        str_to_opcodes.put("ge", Mnemonic.GE);
        str_to_opcodes.put("eq", Mnemonic.EQ);
        str_to_opcodes.put("ne", Mnemonic.NE);
        str_to_opcodes.put("and", Mnemonic.AND);
        str_to_opcodes.put("or", Mnemonic.OR);
        str_to_opcodes.put("not", Mnemonic.NOT);
        str_to_opcodes.put("neg", Mnemonic.NEG);
        str_to_opcodes.put("jmp", Mnemonic.JMP);
        str_to_opcodes.put("jz", Mnemonic.JZ);
        str_to_opcodes.put("prtc", Mnemonic.PRTC);
        str_to_opcodes.put("prts", Mnemonic.PRTS);
        str_to_opcodes.put("prti", Mnemonic.PRTI);
        str_to_opcodes.put("prts", Mnemonic.PRTS);
        str_to_opcodes.put("halt", Mnemonic.HALT);
        
        if (args.length > 0) {
            try {
                run(load_code("C:\\Users\\Swen\\Documents\\NetBeansProjects\\test.rc"));

            } catch (FileNotFoundException e) {
                System.out.println("Ex: "+e);//.getMessage());
            }
        }
    }
}
