package jp.synthtarou.midinet.libs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * ストリームをテキストファイルの行としてあつかうクラス（書き込み用）
 * @author Syntarou YOSHIDA
 */
public class MXLineWriter {
    OutputStream _fout;
    PrintWriter _writer;
    
    String _charset;
   
    public MXLineWriter(File file, String charset) throws IOException {
        _fout = new FileOutputStream(file);
        try {
            _writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(_fout, charset)));
        }catch(IOException ioe) {
            try {
                _fout.close();
            }catch(IOException e) {
            }
            _fout = null;
            throw ioe;
        }
        _writer.println("#charset=" + charset);
    }
    
    public void close() throws IOException {
        if (_writer != null) {
            _writer.flush();
            _writer.close();
            _writer = null;
        }
    }
    
    public void println(String text) {
        _writer.println(text);
    }

    public PrintWriter getWriter() {
        return _writer;
    }
    
}
