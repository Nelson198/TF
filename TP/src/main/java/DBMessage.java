public class DBMessage implements Message {
    // ..... contents of the database

    public String getType() {
        return "db";
    }
}
