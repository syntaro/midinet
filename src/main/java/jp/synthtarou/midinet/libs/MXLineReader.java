package jp.synthtarou.midinet.libs;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

/**
 * ストリームをテキストファイルの行としてあつかうクラス（読み込み用）
 * @author Syntarou YOSHIDA
 */
public class MXLineReader {
    InputStream _in;
    String _charset;
    byte[] _buffer;
    int _pos;
    int _bufferSize;
    boolean _notYetData = true;

    static String TAG = "MXLineReader";
    
    public MXLineReader(InputStream in) {
        this(in, "utf-8");
    }
    public MXLineReader(InputStream in, String charset) {
        if (in == null) {
            throw new NullPointerException("MXLineReader args Null");
        }
        _in = in;
        _charset = charset;
        _buffer = new byte[4096];
        _pos = 0;
        _bufferSize = 0;
    }
    
    protected boolean fetchIfNeed() throws IOException {
        if (_pos >= _bufferSize) {
            int newSize = _in.read(_buffer, 0, _buffer.length);
            if (newSize <= 0) {
                return false;
            }
            _bufferSize = newSize;
            _pos = 0;
        }
        return true;
    }

    public String readLine() throws IOException {
        byte[] line = new byte[256];
        int x = 0;
        boolean fetched = false;
        
        do {
            if (_pos >= _bufferSize && fetchIfNeed() == false) {
                break;
            }
            byte ch = _buffer[_pos ++ ];
            fetched = true;
            if (ch == '\r') continue;
            if (ch == '\n') break;
            line[x ++] = ch;
            if (x >= line.length) {
                byte[] newLine = new byte[line.length * 2];
                System.arraycopy(line, 0, newLine, 0, line.length);
                line = newLine;
            }
        }while(true);
        
        if (!fetched) {
            return null;
        }
        
        String text = new String(line, 0, x, "ASCII");
        //<?xml version="1.0" encoding="Shift_JIS"?>
        if (_notYetData) {
            if (text.startsWith("<?xml ")) {
                String target = "encoding=";
                int z= text.indexOf(target);
                if (z >= 1) {
                    String enc=  text.substring(z + target.length() + 1);
                    int y = enc.indexOf('"');
                    if (y >= 1) {
                        enc = enc.substring(0, y);
                    }
                    _charset = enc;
                }
            }
            else if (text.toLowerCase().startsWith("#charset=")) {
                String charset = text.substring("#charset=".length());
                if (charset.length() > 0) {
                    _charset = charset;
                }
                _notYetData = false;
                return readLine();
            }
            else {
                _notYetData = false;
            }
        }
        try {
            text = new String(line, 0, x, _charset);
        }catch(Exception ex) {
            Log.w("-", ex.getMessage(), ex);
        }
        return text;
    }

    public String reedAll() {
        StringBuffer str = new StringBuffer();
        while (true) {
            try {
                String line = readLine();
                if (line == null) {
                    break;
                }
                str.append(line);
                str.append("\n");
            } catch (IOException ex) {
                break;
            }
        }
        return str.toString();
    }
}
