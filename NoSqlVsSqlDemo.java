import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.regex.*;

/** One-file, in-memory teaching simulator for relational SQL and document NoSQL models. */
public class NoSqlVsSqlDemo extends JFrame {
    private final JTextField idField = new JTextField("3", 5);
    private final JTextField nameField = new JTextField("Lan", 9);
    private final JTextField subjectField = new JTextField("DBMS", 9);
    private final JTextField scoreField = new JTextField("9", 4);
    private final JTextField queryField = new JTextField(10);
    private final JTextField extraField = new JTextField("hobby", 8);
    private final JTextField extraValue = new JTextField("Đọc sách", 10);

    private final DefaultTableModel sqlModel = model("Student ID", "Name", "Subject", "Score");
    private final DefaultTableModel mongoModel = model("_id", "Document JSON");
    private final JTextArea sqlCommand = commandArea();
    private final JTextArea mongoCommand = commandArea();
    private final JTextArea sqlInput = new JTextArea();
    private final JTextArea mongoOutput = commandArea();
    private final JTextArea explanation = new JTextArea();
    private final JTextArea log = new JTextArea(4, 80);

    // Simulated SQL tables: Students and Scores are kept separately and joined for display.
    private final LinkedHashMap<Integer, String> sqlStudents = new LinkedHashMap<>();
    private final ArrayList<SqlScore> sqlScores = new ArrayList<>();
    // Simulated Mongo collection: each student document embeds its scores array.
    private final LinkedHashMap<Integer, LinkedHashMap<String, Object>> mongoStudents = new LinkedHashMap<>();

    private static final class SqlScore {
        final int studentId;
        final String subject;
        double score;
        SqlScore(int studentId, String subject, double score) {
            this.studentId = studentId; this.subject = subject; this.score = score;
        }
    }

    public NoSqlVsSqlDemo() {
        super("NoSQL vs SQL — Database Visual Lab (Mô phỏng)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1360, 900);
        setMinimumSize(new Dimension(1050, 700));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));
        root.setBackground(new Color(245, 248, 252));
        setContentPane(root);

        JLabel title = new JLabel("NoSQL vs SQL — Database Visual Lab", JLabel.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 23));
        root.add(title, BorderLayout.NORTH);

        JPanel main = new JPanel(new BorderLayout(8, 8));
        main.setOpaque(false);
        root.add(main, BorderLayout.CENTER);

        JPanel work = new JPanel(new BorderLayout(8, 8));
        work.setOpaque(false);
        main.add(work, BorderLayout.CENTER);

        JPanel actions = new JPanel(new BorderLayout(0, 3));
        actions.setBorder(BorderFactory.createTitledBorder("Thao tác trên hai mô hình mô phỏng"));
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        for (Component c : new Component[]{new JLabel("ID"), idField, new JLabel("Tên"), nameField,
                new JLabel("Môn"), subjectField, new JLabel("Điểm"), scoreField}) controls.add(c);
        controls.add(button("INSERT", () -> crud("insert")));
        controls.add(button("UPDATE", () -> crud("update")));
        controls.add(button("DELETE", () -> crud("delete")));
        controls.add(new JLabel("Tìm tên")); controls.add(queryField);
        controls.add(button("QUERY", this::refresh));
        controls.add(button("GROUP BY / AGGREGATE", this::aggregateScores));
        actions.add(controls, BorderLayout.NORTH);

        JPanel addField = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addField.add(new JLabel("Field riêng cho document:")); addField.add(extraField);
        addField.add(new JLabel("Giá trị:")); addField.add(extraValue);
        addField.add(button("THÊM FIELD NoSQL", this::addNoSqlField));
        actions.add(addField, BorderLayout.SOUTH);
        work.add(actions, BorderLayout.NORTH);

        JPanel tables = new JPanel(new GridLayout(1, 2, 8, 0));
        tables.setOpaque(false);
        tables.add(tableBox("SQL Server · Students JOIN Scores (mô phỏng)", new JTable(sqlModel)));
        tables.add(tableBox("MongoDB · students collection (mô phỏng)", new JTable(mongoModel)));

        JPanel commandPanels = new JPanel(new GridLayout(1, 2, 8, 0));
        commandPanels.add(textBox("LỆNH SQL SERVER · minh họa", sqlCommand));
        commandPanels.add(textBox("LỆNH MONGODB · minh họa", mongoCommand));

        explanation.setEditable(false);
        explanation.setLineWrap(true);
        explanation.setWrapStyleWord(true);
        explanation.setFont(new Font("SansSerif", Font.PLAIN, 14));
        explanation.setText("MÔ HÌNH LƯU TRỮ\n"
                + "SQL Server: Students và Scores là hai bảng riêng; StudentId nối chúng thành quan hệ.\n"
                + "MongoDB: mỗi sinh viên là một document; điểm có thể nhúng trong mảng scores.\n\n"
                + "TRUY VẤN VÀ CẤU TRÚC\n"
                + "SQL dùng SELECT/JOIN và GROUP BY. MongoDB dùng find() và aggregation pipeline.\n"
                + "MongoDB document có thể có field khác nhau; SQL thường thay đổi cấu trúc bảng bằng DDL.\n\n"
                + "GIỚI HẠN CỦA DEMO\n"
                + "Các thao tác cập nhật dữ liệu mẫu trong bộ nhớ Java. Câu lệnh hiển thị chỉ để học và so sánh, không được gửi tới SQL Server hay MongoDB. Không mô phỏng hiệu năng, phân quyền, giao dịch phân tán, replication hoặc sharding.");

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Câu lệnh đang dùng", commandPanels);
        tabs.addTab("Giải thích khác biệt", new JScrollPane(explanation));
        tabs.addTab("Toán tử MongoDB ↔ SQL", operatorPanel());
        tabs.addTab("Viết SQL → NoSQL", translatorPanel());

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tables, tabs);
        split.setResizeWeight(.68);
        split.setOneTouchExpandable(true);
        split.setContinuousLayout(true);
        split.setDividerSize(8);
        split.setBorder(null);
        split.setDividerLocation(.68);
        work.add(split, BorderLayout.CENTER);

        log.setEditable(false);
        log.setLineWrap(true);
        log.setWrapStyleWord(true);
        JScrollPane status = new JScrollPane(log);
        status.setBorder(BorderFactory.createTitledBorder("Truy vấn và trạng thái · mô phỏng"));
        root.add(status, BorderLayout.SOUTH);

        seedData();
        refresh();
        say("Đang chạy hoàn toàn trong bộ nhớ Java; không cần cài hoặc khởi động SQL Server/MongoDB.");
    }

    private static DefaultTableModel model(String... headers) {
        return new DefaultTableModel(headers, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    private static JTextArea commandArea() {
        JTextArea area = new JTextArea(4, 42);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        return area;
    }

    private JButton button(String label, Runnable action) {
        JButton b = new JButton(label);
        b.setBackground(new Color(37, 99, 235));
        b.setForeground(Color.WHITE);
        b.addActionListener(e -> {
            try { action.run(); }
            catch (Exception ex) {
                String message = ex.getMessage() == null ? ex.toString() : ex.getMessage();
                say("LỖI: " + message);
                JOptionPane.showMessageDialog(this, message, "Lỗi mô phỏng", JOptionPane.ERROR_MESSAGE);
            }
        });
        return b;
    }

    private static JPanel tableBox(String title, JTable table) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        table.setRowHeight(25);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private static JPanel textBox(String title, JTextArea text) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.add(new JScrollPane(text), BorderLayout.CENTER);
        return panel;
    }

    private JPanel operatorPanel() {
        String[] headers = {"Nhóm", "Toán tử MongoDB", "Ý nghĩa", "Đối chiếu SQL Server"};
        DefaultTableModel m = model(headers);
        Object[][] rows = {
                {"Read · so sánh", "$eq, $ne, $gt, $gte, $lt, $lte", "So sánh field với giá trị.", "=, <>, >, >=, <, <= · WHERE Score >= 8"},
                {"Read · tập hợp", "$in, $nin", "Kiểm tra field có thuộc danh sách.", "IN / NOT IN · WHERE StudentId IN (1, 2)"},
                {"Read · logic", "$and, $or, $nor, $not", "Kết hợp hoặc phủ định điều kiện.", "AND, OR, NOT · NOT (A OR B)"},
                {"Update · field", "$set, $unset, $inc", "Gán, bỏ field hoặc tăng/giảm số.", "UPDATE SET col=value; col=col+delta"},
                {"Update · mảng", "$push, $pull, $addToSet", "Thêm, xóa hoặc thêm phần tử duy nhất vào mảng.", "Thường dùng bảng con, INSERT/DELETE và UNIQUE"},
                {"Delete", "deleteOne(), deleteMany()", "Xóa một hoặc nhiều document khớp điều kiện.", "DELETE FROM table WHERE ..."}
        };
        for (Object[] row : rows) m.addRow(row);
        JTable table = new JTable(m);
        table.setRowHeight(56);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        int[] widths = {120, 190, 380, 420};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        JLabel note = new JLabel("Bảng tham khảo cú pháp; ứng dụng không gửi các lệnh này tới database thật.");
        note.setBorder(new EmptyBorder(5, 7, 4, 7));
        panel.add(note, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel translatorPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        JPanel toolbar = new JPanel(new BorderLayout(8, 0));
        JLabel note = new JLabel("Nhập SELECT đơn giản từ Students; hỗ trợ WHERE với StudentId, Name, Subject, Score và AND.");
        toolbar.add(note, BorderLayout.CENTER);
        toolbar.add(button("DỊCH SQL → MONGODB & LỌC", this::translateSqlToMongo), BorderLayout.EAST);
        panel.add(toolbar, BorderLayout.NORTH);

        sqlInput.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        sqlInput.setLineWrap(true);
        sqlInput.setWrapStyleWord(true);
        sqlInput.setText("SELECT s.StudentId, s.Name, c.Subject, c.Score\n"
                + "FROM Students s LEFT JOIN Scores c ON c.StudentId=s.StudentId\n"
                + "WHERE s.Name LIKE '%Lan%';");
        mongoOutput.setText("Bản dịch MongoDB sẽ hiện ở đây.");
        JSplitPane editors = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                textBox("TRUY VẤN SQL BẠN NHẬP", sqlInput), textBox("MONGODB TƯƠNG ĐƯƠNG (MÔ PHỎNG)", mongoOutput));
        editors.setResizeWeight(.5);
        editors.setDividerLocation(.5);
        panel.add(editors, BorderLayout.CENTER);
        return panel;
    }

    private static final class QueryCondition {
        final String sqlField, mongoField, operator;
        final Object value;
        QueryCondition(String sqlField, String mongoField, String operator, Object value) {
            this.sqlField = sqlField; this.mongoField = mongoField; this.operator = operator; this.value = value;
        }
    }

    private void translateSqlToMongo() {
        String sql = sqlInput.getText().trim();
        if (!sql.regionMatches(true, 0, "SELECT", 0, 6) || !Pattern.compile("(?i)\\bFROM\\s+(?:\\[?dbo\\]?\\.)?\\[?Students\\]?\\b").matcher(sql).find())
            throw new IllegalArgumentException("Bản dịch hiện hỗ trợ SELECT ... FROM Students, có thể JOIN Scores.");
        Matcher whereMatcher = Pattern.compile("(?is)\\bWHERE\\b(.+)$").matcher(sql);
        String where = whereMatcher.find() ? whereMatcher.group(1).replaceFirst(";\\s*$", "").trim() : "";
        where = where.replaceFirst("(?is)\\s+ORDER\\s+BY\\s+.+$", "").trim();
        if (where.toUpperCase(Locale.ROOT).contains(" OR "))
            throw new IllegalArgumentException("Bản dịch bản này hỗ trợ điều kiện AND; OR chưa được hỗ trợ.");

        ArrayList<QueryCondition> conditions = new ArrayList<>();
        if (!where.isBlank()) {
            Pattern conditionPattern = Pattern.compile("(?i)^(?:[A-Za-z_]\\w*\\.)?\\[?(StudentId|Name|Subject|Score)\\]?\\s*(LIKE|>=|<=|<>|=|>|<)\\s*(?:N)?(?:'((?:''|[^'])*)'|(-?\\d+(?:\\.\\d+)?))$");
            for (String part : where.split("(?i)\\s+AND\\s+")) {
                Matcher m = conditionPattern.matcher(part.trim());
                if (!m.matches()) throw new IllegalArgumentException("Điều kiện chưa hỗ trợ: " + part.trim());
                String field = m.group(1);
                String op = m.group(2).toUpperCase(Locale.ROOT);
                Object value = m.group(3) != null ? m.group(3).replace("''", "'") : Double.valueOf(m.group(4));
                String mongoField = switch (field.toLowerCase(Locale.ROOT)) {
                    case "studentid" -> "_id";
                    case "name" -> "name";
                    case "subject" -> "scores.subject";
                    default -> "scores.score";
                };
                if (op.equals("LIKE") && !(value instanceof String)) throw new IllegalArgumentException("LIKE chỉ dùng với Name hoặc Subject.");
                conditions.add(new QueryCondition(field, mongoField, op, value));
            }
        }

        String filter = mongoFilter(conditions);
        boolean joinsScores = Pattern.compile("(?i)\\bJOIN\\s+Scores\\b").matcher(sql).find();
        String comment = joinsScores ? "// JOIN Scores được ánh xạ sang mảng scores nhúng trong document.\n" : "";
        String translated = comment + "db.students.find(" + filter + ").pretty();\n\n"
                + "// Đây là bản dịch giới hạn cho cú pháp SELECT/WHERE được hỗ trợ; chỉ mô phỏng trong bộ nhớ.";
        mongoOutput.setText(translated);
        applyTranslatedFilter(conditions);
        say("Đã dịch truy vấn SELECT đơn giản sang MongoDB find() và lọc dữ liệu mô phỏng.");
    }

    private static String mongoFilter(List<QueryCondition> conditions) {
        if (conditions.isEmpty()) return "{}";
        ArrayList<String> clauses = new ArrayList<>();
        for (QueryCondition c : conditions) {
            if (c.operator.equals("LIKE")) {
                clauses.add(json(c.mongoField) + ": /" + sqlLikeRegex(String.valueOf(c.value)) + "/i");
            } else if (c.operator.equals("=")) {
                clauses.add(json(c.mongoField) + ": " + mongoLiteral(c.value));
            } else {
                String op = switch (c.operator) {
                    case "<>" -> "$ne"; case ">" -> "$gt"; case ">=" -> "$gte";
                    case "<" -> "$lt"; case "<=" -> "$lte";
                    default -> throw new IllegalArgumentException("Toán tử chưa hỗ trợ: " + c.operator);
                };
                clauses.add(json(c.mongoField) + ": { " + op + ": " + mongoLiteral(c.value) + " }");
            }
        }
        return clauses.size() == 1 ? "{ " + clauses.get(0) + " }" : "{ $and: [{ " + String.join(" }, { ", clauses) + " }] }";
    }

    private static String mongoLiteral(Object value) {
        return value instanceof String s ? json(s) : String.valueOf(value);
    }

    private static String sqlLikeRegex(String pattern) {
        StringBuilder regex = new StringBuilder();
        for (char ch : pattern.toCharArray()) {
            if (ch == '%') regex.append(".*");
            else if (ch == '_') regex.append('.');
            else if ("\\.^$|?*+()[]{}".indexOf(ch) >= 0) regex.append('\\').append(ch);
            else regex.append(ch);
        }
        return regex.toString();
    }

    private void applyTranslatedFilter(List<QueryCondition> conditions) {
        sqlModel.setRowCount(0); mongoModel.setRowCount(0);
        for (Map.Entry<Integer, String> student : sqlStudents.entrySet()) {
            boolean emitted = false;
            for (SqlScore row : sqlScores) {
                if (row.studentId != student.getKey() || !matchesSqlRow(student.getKey(), student.getValue(), row, conditions)) continue;
                sqlModel.addRow(new Object[]{student.getKey(), student.getValue(), row.subject, row.score});
                emitted = true;
            }
            if (!emitted && sqlScores.stream().noneMatch(r -> r.studentId == student.getKey())
                    && matchesSqlRow(student.getKey(), student.getValue(), null, conditions))
                sqlModel.addRow(new Object[]{student.getKey(), student.getValue(), "", ""});
        }
        for (Map<String, Object> document : mongoStudents.values())
            if (matchesMongoDocument(document, conditions)) mongoModel.addRow(new Object[]{document.get("_id"), json(document)});
    }

    private boolean matchesSqlRow(int studentId, String studentName, SqlScore row, List<QueryCondition> conditions) {
        for (QueryCondition c : conditions) {
            Object actual = switch (c.sqlField.toLowerCase(Locale.ROOT)) {
                case "studentid" -> studentId;
                case "name" -> studentName;
                case "subject" -> row == null ? null : row.subject;
                default -> row == null ? null : row.score;
            };
            if (!matchesValue(actual, c.operator, c.value)) return false;
        }
        return true;
    }

    private boolean matchesMongoDocument(Map<String, Object> document, List<QueryCondition> conditions) {
        for (QueryCondition c : conditions) {
            if (c.mongoField.equals("_id") && !matchesValue(document.get("_id"), c.operator, c.value)) return false;
            if (c.mongoField.equals("name") && !matchesValue(document.get("name"), c.operator, c.value)) return false;
            if (c.mongoField.startsWith("scores.")) {
                String key = c.mongoField.substring("scores.".length());
                boolean found = false;
                for (Map<String, Object> scoreDoc : scoresOf(document))
                    if (matchesValue(scoreDoc.get(key), c.operator, c.value)) { found = true; break; }
                if (!found) return false;
            }
        }
        return true;
    }

    private static boolean matchesValue(Object actual, String operator, Object expected) {
        if (actual == null) return false;
        if (operator.equals("LIKE")) {
            int flags = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
            return Pattern.compile(sqlLikeRegex(String.valueOf(expected)), flags).matcher(String.valueOf(actual)).matches();
        }
        int comparison;
        if (actual instanceof Number a && expected instanceof Number b) comparison = Double.compare(a.doubleValue(), b.doubleValue());
        else comparison = String.valueOf(actual).compareToIgnoreCase(String.valueOf(expected));
        return switch (operator) {
            case "=" -> comparison == 0; case "<>" -> comparison != 0; case ">" -> comparison > 0;
            case ">=" -> comparison >= 0; case "<" -> comparison < 0; case "<=" -> comparison <= 0;
            default -> false;
        };
    }

    private void seedData() {
        sqlStudents.clear(); sqlScores.clear(); mongoStudents.clear();
        addSeed(1, "Lan", "DBMS", 9);
        addSeed(1, "Lan", "Java", 8.5);
        addSeed(2, "Minh", "DBMS", 8);
    }

    private void addSeed(int id, String name, String subject, double score) {
        sqlStudents.put(id, name);
        sqlScores.add(new SqlScore(id, subject, score));
        LinkedHashMap<String, Object> doc = mongoStudents.computeIfAbsent(id, key -> {
            LinkedHashMap<String, Object> d = new LinkedHashMap<>();
            d.put("_id", key); d.put("name", name); d.put("scores", new ArrayList<LinkedHashMap<String, Object>>());
            return d;
        });
        scoresOf(doc).add(scoreDocument(subject, score));
    }

    private static LinkedHashMap<String, Object> scoreDocument(String subject, double score) {
        LinkedHashMap<String, Object> d = new LinkedHashMap<>();
        d.put("subject", subject); d.put("score", score); return d;
    }

    @SuppressWarnings("unchecked")
    private static ArrayList<LinkedHashMap<String, Object>> scoresOf(Map<String, Object> doc) {
        return (ArrayList<LinkedHashMap<String, Object>>) doc.get("scores");
    }

    private void crud(String operation) {
        int studentId = Integer.parseInt(idField.getText().trim());
        String studentName = nameField.getText().trim();
        String subject = subjectField.getText().trim();
        double grade = Double.parseDouble(scoreField.getText().trim());
        if (studentId < 1) throw new IllegalArgumentException("ID phải là số nguyên dương.");
        if (studentName.isBlank() || subject.isBlank()) throw new IllegalArgumentException("Tên và môn học không được để trống.");
        if (!Double.isFinite(grade) || grade < 0 || grade > 10) throw new IllegalArgumentException("Điểm phải nằm từ 0 đến 10.");

        if (operation.equals("insert")) {
            if (sqlStudents.containsKey(studentId) || mongoStudents.containsKey(studentId))
                throw new IllegalArgumentException("ID " + studentId + " đã tồn tại. Hãy chọn ID khác hoặc dùng UPDATE.");
            sqlStudents.put(studentId, studentName);
            sqlScores.add(new SqlScore(studentId, subject, grade));
            LinkedHashMap<String, Object> d = new LinkedHashMap<>();
            d.put("_id", studentId); d.put("name", studentName);
            ArrayList<LinkedHashMap<String, Object>> scores = new ArrayList<>();
            scores.add(scoreDocument(subject, grade)); d.put("scores", scores);
            mongoStudents.put(studentId, d);
            setCommands("-- Mô phỏng INSERT vào hai bảng\nINSERT INTO dbo.Students(StudentId, Name) VALUES (" + studentId + ", N'" + sqlQuote(studentName) + "');\nINSERT INTO dbo.Scores(StudentId, Subject, Score) VALUES (" + studentId + ", N'" + sqlQuote(subject) + "', " + grade + ");",
                    "// Mô phỏng upsert document và thêm điểm\ndb.students.insertOne({ _id: " + studentId + ", name: " + json(studentName) + ", scores: [{ subject: " + json(subject) + ", score: " + grade + " }] });");
        } else if (operation.equals("update")) {
            if (!sqlStudents.containsKey(studentId) || !mongoStudents.containsKey(studentId))
                throw new IllegalArgumentException("Không tìm thấy sinh viên ID " + studentId + " để UPDATE.");
            sqlStudents.put(studentId, studentName);
            for (SqlScore row : sqlScores) if (row.studentId == studentId && row.subject.equalsIgnoreCase(subject)) row.score = grade;
            LinkedHashMap<String, Object> d = mongoStudents.get(studentId);
            d.put("name", studentName);
            for (Map<String, Object> row : scoresOf(d)) if (subject.equalsIgnoreCase(String.valueOf(row.get("subject")))) row.put("score", grade);
            setCommands("-- Mô phỏng UPDATE theo StudentId và môn học\nUPDATE dbo.Students SET Name=N'" + sqlQuote(studentName) + "' WHERE StudentId=" + studentId + ";\nUPDATE dbo.Scores SET Score=" + grade + " WHERE StudentId=" + studentId + " AND Subject=N'" + sqlQuote(subject) + "';",
                    "// Mô phỏng updateOne + $set\ndb.students.updateOne({ _id: " + studentId + " }, { $set: { name: " + json(studentName) + ", 'scores.$[s].score': " + grade + " } }, { arrayFilters: [{ 's.subject': " + json(subject) + " }] });");
        } else {
            if (!sqlStudents.containsKey(studentId) && !mongoStudents.containsKey(studentId))
                throw new IllegalArgumentException("Không tìm thấy sinh viên ID " + studentId + " để DELETE.");
            sqlStudents.remove(studentId);
            sqlScores.removeIf(row -> row.studentId == studentId);
            mongoStudents.remove(studentId);
            setCommands("-- Mô phỏng xóa bảng con trước, bảng cha sau\nDELETE FROM dbo.Scores WHERE StudentId=" + studentId + ";\nDELETE FROM dbo.Students WHERE StudentId=" + studentId + ";",
                    "// Mô phỏng xóa một document\ndb.students.deleteOne({ _id: " + studentId + " });");
        }
        refreshTables();
        say(operation.toUpperCase(Locale.ROOT) + " đã cập nhật cả mô hình bảng SQL và document MongoDB trong bộ nhớ Java.");
    }

    private void addNoSqlField() {
        int studentId = Integer.parseInt(idField.getText().trim());
        String field = extraField.getText().trim();
        String value = extraValue.getText();
        if (!field.matches("[A-Za-z][A-Za-z0-9_]*") || field.equals("_id") || field.equals("scores"))
            throw new IllegalArgumentException("Tên field phải bắt đầu bằng chữ và không được là _id hoặc scores.");
        LinkedHashMap<String, Object> d = mongoStudents.get(studentId);
        if (d == null) throw new IllegalArgumentException("MongoDB mô phỏng không có document ID " + studentId + ".");
        d.put(field, value);
        setCommands("-- MongoDB cho document field linh hoạt.\n-- Nếu chuẩn hóa thêm cột cho toàn bảng SQL:\nALTER TABLE dbo.Students ADD [" + field + "] NVARCHAR(100) NULL;\n-- Đây chỉ là lệnh minh họa, không thực thi.",
                "// Mô phỏng thêm field riêng cho một document\ndb.students.updateOne({ _id: " + studentId + " }, { $set: { " + field + ": " + json(value) + " } });");
        refreshTables();
        say("Đã thêm field '" + field + "' vào document mô phỏng ID " + studentId + ". Cấu trúc bảng SQL không đổi.");
    }

    private void refresh() {
        String q = queryField.getText().trim();
        setCommands("-- Mô phỏng SELECT + LEFT JOIN, lọc tên\nSELECT s.StudentId, s.Name, c.Subject, c.Score\nFROM dbo.Students s LEFT JOIN dbo.Scores c ON c.StudentId=s.StudentId\nWHERE s.Name LIKE N'%" + sqlQuote(q) + "%';",
                "// Mô phỏng truy vấn document theo regex\ndb.students.find({ name: /" + q.replace("/", "\\/") + "/i }).pretty();");
        refreshTables();
        say("QUERY mô phỏng: SQL JOIN trả các hàng phù hợp; MongoDB find() trả document phù hợp.");
    }

    private void refreshTables() {
        sqlModel.setRowCount(0); mongoModel.setRowCount(0);
        String q = queryField.getText().trim().toLowerCase(Locale.ROOT);
        for (Map.Entry<Integer, String> student : sqlStudents.entrySet()) {
            if (!student.getValue().toLowerCase(Locale.ROOT).contains(q)) continue;
            boolean hasScore = false;
            for (SqlScore row : sqlScores) if (row.studentId == student.getKey()) {
                sqlModel.addRow(new Object[]{student.getKey(), student.getValue(), row.subject, row.score}); hasScore = true;
            }
            if (!hasScore) sqlModel.addRow(new Object[]{student.getKey(), student.getValue(), "", ""});
        }
        for (Map<String, Object> doc : mongoStudents.values()) {
            String studentName = String.valueOf(doc.get("name"));
            if (studentName.toLowerCase(Locale.ROOT).contains(q)) mongoModel.addRow(new Object[]{doc.get("_id"), json(doc)});
        }
    }

    private void aggregateScores() {
        TreeMap<String, double[]> groups = new TreeMap<>();
        for (SqlScore row : sqlScores) {
            double[] a = groups.computeIfAbsent(row.subject, ignored -> new double[2]);
            a[0] += row.score; a[1]++;
        }
        sqlModel.setRowCount(0); mongoModel.setRowCount(0);
        for (Map.Entry<String, double[]> entry : groups.entrySet()) {
            double average = entry.getValue()[0] / entry.getValue()[1];
            int count = (int) entry.getValue()[1];
            sqlModel.addRow(new Object[]{"GROUP", count + " điểm", entry.getKey(), String.format(Locale.US, "%.2f", average)});
            LinkedHashMap<String, Object> groupDoc = new LinkedHashMap<>();
            groupDoc.put("_id", entry.getKey()); groupDoc.put("averageScore", Math.round(average * 100.0) / 100.0); groupDoc.put("scoreCount", count);
            mongoModel.addRow(new Object[]{entry.getKey(), json(groupDoc)});
        }
        setCommands("-- Mô phỏng GROUP BY trên bảng Scores\nSELECT Subject, AVG(Score) AS AverageScore, COUNT(*) AS ScoreCount\nFROM dbo.Scores GROUP BY Subject ORDER BY Subject;",
                "// Mô phỏng aggregation pipeline trên mảng scores\ndb.students.aggregate([\n  { $unwind: '$scores' },\n  { $group: { _id: '$scores.subject', averageScore: { $avg: '$scores.score' }, scoreCount: { $sum: 1 } } },\n  { $sort: { _id: 1 } }\n]);");
        say("GROUP BY / AGGREGATE mô phỏng: so sánh kết quả tổng hợp theo môn ở hai mô hình.");
    }

    private void setCommands(String sql, String mongo) { sqlCommand.setText(sql); mongoCommand.setText(mongo); }
    private void say(String message) { log.append("• " + message + "\n"); log.setCaretPosition(log.getDocument().getLength()); }
    private static String sqlQuote(String value) { return value.replace("'", "''"); }

    private static String json(Object value) {
        if (value == null) return "null";
        if (value instanceof String s) return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
        if (value instanceof Map<?, ?> map) {
            ArrayList<String> parts = new ArrayList<>();
            map.forEach((k, v) -> parts.add(json(String.valueOf(k)) + ": " + json(v)));
            return "{" + String.join(", ", parts) + "}";
        }
        if (value instanceof Iterable<?> items) {
            ArrayList<String> parts = new ArrayList<>();
            for (Object item : items) parts.add(json(item));
            return "[" + String.join(", ", parts) + "]";
        }
        return String.valueOf(value);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new NoSqlVsSqlDemo().setVisible(true));
    }
}
