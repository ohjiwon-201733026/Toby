package springbook.learningtest.spring.factorybean;

public class Message {
    String text;
    
    
    // 생성자가 private -> 외부에서 생성자를 통해 오브젝트 생성 X
    private Message(String text){
        this.text=text;
    }

    public String getText(){
        return text;
    }
    // 생성자 대신 사용할 수 있는 static factory 메소드
    public static Message newMessage(String text){
        return new Message(text);
    }
}
