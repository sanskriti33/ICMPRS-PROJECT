import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Main extends JFrame {
    private static final Color BACKGROUND = new Color(239, 244, 250);
    private static final Color PANEL = Color.WHITE;
    private static final Color NAVY = new Color(27, 50, 79);
    private static final Color TEAL = new Color(34, 139, 110);
    private static final Color ORANGE = new Color(238, 132, 63);
    private static final Color RED = new Color(211, 67, 67);
    private static final Color BLACK_BUTTON = new Color(25, 25, 25);
    private static final Color GOLD = new Color(232, 181, 73);
    private static final Color SOFT_BLUE = new Color(232, 240, 250);

    private final ComplaintManager manager = new ComplaintManager();
    private final ComplaintTableModel tableModel = new ComplaintTableModel();
    private final JTable complaintTable = new JTable(tableModel);
    private final JTextField idField = new JTextField();
    private final JTextArea descriptionArea = new JTextArea(6, 30);
    private final JLabel priorityPreview = new JLabel("Priority: 0 | Level: - | Department: -");
    private final JTextField searchField = new JTextField();
    private final JComboBox<String> statusBox = new JComboBox<>(new String[]{"Open", "In Progress", "Resolved", "Closed"});
    private final JLabel storageLabel = new JLabel();
    private final JLabel totalCard = new JLabel();
    private final JLabel highCard = new JLabel();
    private final JLabel openCard = new JLabel();
    private final JLabel resolvedCard = new JLabel();

    public Main() {
        super("ICMPRS - Customer Complaint Portal");
        configureFrame();
        buildInterface();
        refreshTable();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Default look and feel is acceptable if the system one is unavailable.
            }
            new Main().setVisible(true);
        });
    }

    private void configureFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1160, 720);
        setMinimumSize(new Dimension(980, 620));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND);
    }

    private void buildInterface() {
        JPanel root = new JPanel(new BorderLayout(18, 18));
        root.setBorder(new EmptyBorder(18, 18, 18, 18));
        root.setBackground(BACKGROUND);

        root.add(createHeader(), BorderLayout.NORTH);
        root.add(createMainSplit(), BorderLayout.CENTER);
        root.add(createFooter(), BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel createHeader() {
        JPanel header = new GradientPanel();
        header.setLayout(new BorderLayout(18, 8));
        header.setBorder(new EmptyBorder(22, 22, 22, 22));

        JLabel title = new JLabel("Customer Complaint Portal");
        title.setFont(new Font("Segoe UI Semibold", Font.BOLD, 26));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Submit complaints, view status, search records, and manage updates in one place.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(228, 236, 246));

        JLabel badge = new JLabel(" Smart Routing + JDBC + Status Tracking ");
        badge.setOpaque(true);
        badge.setBackground(new Color(255, 255, 255, 35));
        badge.setForeground(Color.WHITE);
        badge.setFont(new Font("Segoe UI Semibold", Font.BOLD, 12));

        JPanel text = new JPanel(new GridLayout(3, 1, 0, 4));
        text.setOpaque(false);
        text.add(title);
        text.add(subtitle);
        text.add(badge);

        storageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        storageLabel.setForeground(new Color(244, 247, 251));
        storageLabel.setText("Storage: " + manager.getStorageName());

        header.add(text, BorderLayout.WEST);
        header.add(storageLabel, BorderLayout.EAST);
        return header;
    }

    private JSplitPane createMainSplit() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createLeftPanel(), createRightPanel());
        splitPane.setResizeWeight(0.34);
        splitPane.setDividerSize(8);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);
        return splitPane;
    }

    private JPanel createLeftPanel() {
        JPanel panel = cardPanel(new BorderLayout(14, 14));

        JLabel heading = sectionTitle("Submit New Complaint");
        panel.add(heading, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        JPanel idPanel = new JPanel(new BorderLayout(8, 4));
        idPanel.setOpaque(false);
        idPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        idPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        idPanel.add(fieldLabel("Complaint ID"), BorderLayout.NORTH);
        styleInputField(idField);
        idPanel.add(idField, BorderLayout.CENTER);

        JPanel descriptionPanel = new JPanel(new BorderLayout(8, 4));
        descriptionPanel.setOpaque(false);
        descriptionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        descriptionPanel.add(fieldLabel("Complaint Details"), BorderLayout.NORTH);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descriptionArea.setPreferredSize(new Dimension(320, 180));
        styleTextArea(descriptionArea);
        descriptionArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updatePreview();
            }
        });
        descriptionPanel.add(createStyledScrollPane(descriptionArea), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new GridLayout(4, 1, 8, 8));
        actionPanel.setOpaque(false);
        actionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 210));
        priorityPreview.setForeground(NAVY);
        priorityPreview.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        JButton addButton = primaryButton("Submit Complaint");
        JButton clearButton = secondaryButton("Clear Form");
        addButton.addActionListener(event -> addComplaint());
        clearButton.addActionListener(event -> clearForm());
        actionPanel.add(priorityPreview);
        actionPanel.add(addButton);
        actionPanel.add(clearButton);
        actionPanel.add(createHintLabel());

        JPanel stepsPanel = createStepsPanel();
        stepsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        stepsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        form.add(idPanel);
        form.add(Box.createRigidArea(new Dimension(0, 12)));
        form.add(descriptionPanel);
        form.add(Box.createRigidArea(new Dimension(0, 12)));
        form.add(stepsPanel);
        form.add(Box.createRigidArea(new Dimension(0, 12)));
        form.add(actionPanel);
        panel.add(form, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setOpaque(false);

        panel.add(createDashboard(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        tabs.addTab("Complaint Records", createTablePanel());
        tabs.addTab("Search / Update", createSearchPanel());

        panel.add(tabs, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createDashboard() {
        JPanel dashboard = new JPanel(new GridLayout(1, 4, 12, 12));
        dashboard.setOpaque(false);
        dashboard.add(statCard(totalCard, "Total Complaints", NAVY));
        dashboard.add(statCard(highCard, "High Priority", RED));
        dashboard.add(statCard(openCard, "Open Cases", ORANGE));
        dashboard.add(statCard(resolvedCard, "Resolved Cases", TEAL));
        return dashboard;
    }

    private JPanel createTablePanel() {
        JPanel panel = cardPanel(new BorderLayout(10, 10));
        panel.add(fieldLabel("All saved complaints are listed here. Select one row to update or delete it."), BorderLayout.NORTH);

        complaintTable.setRowHeight(34);
        complaintTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        complaintTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        complaintTable.getTableHeader().setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        complaintTable.getTableHeader().setForeground(Color.WHITE);
        complaintTable.getTableHeader().setBackground(new Color(18, 35, 58));
        complaintTable.getTableHeader().setOpaque(true);
        complaintTable.getTableHeader().setReorderingAllowed(false);
        complaintTable.getTableHeader().setDefaultRenderer(new HeaderRenderer());
        complaintTable.setGridColor(new Color(226, 233, 242));
        complaintTable.setSelectionBackground(new Color(214, 231, 255));
        complaintTable.setSelectionForeground(NAVY);
        complaintTable.setDefaultRenderer(Object.class, new PriorityRenderer());

        JPanel actions = new JPanel(new GridLayout(1, 4, 10, 10));
        actions.setOpaque(false);
        JButton refresh = secondaryButton("Refresh");
        JButton update = successButton("Update Selected Status");
        JButton delete = dangerButton("Delete Selected");
        JButton sample = secondaryButton("Add Sample");
        refresh.addActionListener(event -> refreshTable());
        update.addActionListener(event -> updateSelectedStatus());
        delete.addActionListener(event -> deleteSelected());
        sample.addActionListener(event -> addSampleComplaint());
        actions.add(refresh);
        actions.add(update);
        actions.add(delete);
        actions.add(sample);

        panel.add(new JScrollPane(complaintTable), BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createSearchPanel() {
        JPanel panel = cardPanel(new BorderLayout(12, 12));

        JPanel top = new JPanel(new GridLayout(2, 2, 10, 10));
        top.setOpaque(false);
        top.add(fieldLabel("Enter Complaint ID"));
        top.add(fieldLabel("Choose New Status"));
        styleInputField(searchField);
        top.add(searchField);
        top.add(statusBox);

        JTextArea resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setBackground(new Color(248, 250, 252));
        styleTextArea(resultArea);

        JPanel actions = new JPanel(new GridLayout(1, 3, 10, 10));
        actions.setOpaque(false);
        JButton search = primaryButton("Find Complaint");
        JButton update = successButton("Change Status");
        JButton delete = dangerButton("Delete");
        search.addActionListener(event -> searchComplaint(resultArea));
        update.addActionListener(event -> updateStatusFromSearch(resultArea));
        delete.addActionListener(event -> deleteFromSearch(resultArea));
        actions.add(search);
        actions.add(update);
        actions.add(delete);

        panel.add(top, BorderLayout.NORTH);
        panel.add(createStyledScrollPane(resultArea), BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        JLabel label = new JLabel("Priority = urgency + impact | Auto-routing: Finance, IT Support, Logistics, Product Team, General Support");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(new Color(88, 102, 120));
        footer.add(label, BorderLayout.WEST);
        return footer;
    }

    private void addComplaint() {
        try {
            int id = parseId(idField.getText());
            String description = descriptionArea.getText().trim();
            if (description.isBlank()) {
                showMessage("Description cannot be empty.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Complaint complaint = new Complaint(id, description);
            manager.addComplaint(complaint);
            showMessage("Complaint submitted successfully.\n" + complaint.getSummary(), "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            refreshTable();
        } catch (NumberFormatException exception) {
            showMessage("Please enter a valid numeric complaint ID.", "Invalid ID", JOptionPane.WARNING_MESSAGE);
        } catch (Exception exception) {
            showMessage(exception.getMessage(), "Unable to Add", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addSampleComplaint() {
        int id = (int) (System.currentTimeMillis() % 100000);
        idField.setText(String.valueOf(id));
        descriptionArea.setText("Urgent payment failed and account is not working for my order.");
        updatePreview();
        addComplaint();
    }

    private void searchComplaint(JTextArea resultArea) {
        try {
            int id = parseId(searchField.getText());
            Optional<Complaint> result = manager.searchComplaint(id);
            resultArea.setText(result.map(this::formatDetails).orElse("Complaint not found."));
        } catch (NumberFormatException exception) {
            showMessage("Please enter a valid numeric complaint ID.", "Invalid ID", JOptionPane.WARNING_MESSAGE);
        } catch (Exception exception) {
            showMessage(exception.getMessage(), "Search Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSelectedStatus() {
        int selectedRow = complaintTable.getSelectedRow();
        if (selectedRow < 0) {
            showMessage("Please select a complaint from the table first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = complaintTable.convertRowIndexToModel(selectedRow);
        Complaint complaint = tableModel.getComplaint(modelRow);
        String selectedStatus = (String) JOptionPane.showInputDialog(
                this,
                "Select new status for complaint #" + complaint.getId(),
                "Update Status",
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[]{"Open", "In Progress", "Resolved", "Closed"},
                complaint.getStatus()
        );
        if (selectedStatus != null) {
            updateStatus(complaint.getId(), selectedStatus);
        }
    }

    private void updateStatusFromSearch(JTextArea resultArea) {
        try {
            int id = parseId(searchField.getText());
            String status = (String) statusBox.getSelectedItem();
            if (updateStatus(id, status)) {
                Optional<Complaint> result = manager.searchComplaint(id);
                resultArea.setText(result.map(this::formatDetails).orElse(""));
            }
        } catch (NumberFormatException exception) {
            showMessage("Please enter a valid numeric complaint ID.", "Invalid ID", JOptionPane.WARNING_MESSAGE);
        } catch (Exception exception) {
            showMessage(exception.getMessage(), "Update Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean updateStatus(int id, String status) {
        try {
            boolean updated = manager.updateStatus(id, status);
            if (updated) {
                showMessage("Status updated successfully.", "Updated", JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
            } else {
                showMessage("Complaint not found.", "Not Found", JOptionPane.WARNING_MESSAGE);
            }
            return updated;
        } catch (Exception exception) {
            showMessage(exception.getMessage(), "Update Failed", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void deleteSelected() {
        int selectedRow = complaintTable.getSelectedRow();
        if (selectedRow < 0) {
            showMessage("Please select a complaint from the table first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = complaintTable.convertRowIndexToModel(selectedRow);
        deleteComplaint(tableModel.getComplaint(modelRow).getId());
    }

    private void deleteFromSearch(JTextArea resultArea) {
        try {
            int id = parseId(searchField.getText());
            if (deleteComplaint(id)) {
                resultArea.setText("Complaint deleted successfully.");
            }
        } catch (NumberFormatException exception) {
            showMessage("Please enter a valid numeric complaint ID.", "Invalid ID", JOptionPane.WARNING_MESSAGE);
        }
    }

    private boolean deleteComplaint(int id) {
        int confirm = JOptionPane.showConfirmDialog(this, "Delete complaint #" + id + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return false;
        }

        try {
            boolean deleted = manager.deleteComplaint(id);
            if (deleted) {
                showMessage("Complaint deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
            } else {
                showMessage("Complaint not found.", "Not Found", JOptionPane.WARNING_MESSAGE);
            }
            return deleted;
        } catch (Exception exception) {
            showMessage(exception.getMessage(), "Delete Failed", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void refreshTable() {
        try {
            List<Complaint> complaints = manager.viewComplaints();
            tableModel.setComplaints(complaints);
            updateDashboard(complaints);
        } catch (Exception exception) {
            showMessage(exception.getMessage(), "Refresh Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateDashboard(List<Complaint> complaints) {
        long high = complaints.stream().filter(Complaint::isHighPriority).count();
        long open = complaints.stream().filter(complaint -> "Open".equalsIgnoreCase(complaint.getStatus())).count();
        long resolved = complaints.stream()
                .filter(complaint -> "Resolved".equalsIgnoreCase(complaint.getStatus()) || "Closed".equalsIgnoreCase(complaint.getStatus()))
                .count();

        totalCard.setText(String.valueOf(complaints.size()));
        highCard.setText(String.valueOf(high));
        openCard.setText(String.valueOf(open));
        resolvedCard.setText(String.valueOf(resolved));
    }

    private void updatePreview() {
        String description = descriptionArea.getText();
        int priority = PriorityCalculator.preview(description);
        priorityPreview.setText("Priority: " + priority + " | Level: " + PriorityCalculator.getPriorityLevel(priority)
                + " | Department: " + DepartmentRouter.detectDepartment(description));
    }

    private String formatDetails(Complaint complaint) {
        return """
                Complaint Found
                ------------------------------
                ID: %d
                Description: %s
                Priority: %d
                Priority Level: %s
                Department: %s
                Status: %s
                High Priority Flag: %s
                Created At: %s
                """.formatted(
                complaint.getId(),
                complaint.getDescription(),
                complaint.getPriority(),
                complaint.getPriorityLevel(),
                complaint.getDepartment(),
                complaint.getStatus(),
                complaint.isHighPriority() ? "1" : "0",
                complaint.getFormattedCreatedAt()
        );
    }

    private void clearForm() {
        idField.setText("");
        descriptionArea.setText("");
        updatePreview();
        Timer focusTimer = new Timer(1, event -> idField.requestFocusInWindow());
        focusTimer.setRepeats(false);
        focusTimer.start();
    }

    private int parseId(String value) {
        int id = Integer.parseInt(value.trim());
        if (id <= 0) {
            throw new NumberFormatException("ID must be positive.");
        }
        return id;
    }

    private JPanel cardPanel(BorderLayout layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(216, 225, 236), 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));
        return panel;
    }

    private JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI Semibold", Font.BOLD, 20));
        label.setForeground(NAVY);
        return label;
    }

    private JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        label.setForeground(new Color(55, 70, 90));
        return label;
    }

    private JLabel createHintLabel() {
        JLabel label = new JLabel("<html>Tip: words like urgent, failed, refund, delivery, login, and damaged help the system calculate priority.</html>");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(new Color(95, 106, 120));
        return label;
    }

    private JPanel createStepsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(246, 249, 253));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(223, 231, 240)),
                new EmptyBorder(10, 12, 10, 12)
        ));

        JLabel title = new JLabel("How To Use");
        title.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        title.setForeground(NAVY);

        JLabel text = new JLabel("<html>1. Enter complaint ID and details.<br>2. Click Submit Complaint.<br>3. Use Search / Update to track or change status.</html>");
        text.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        text.setForeground(new Color(76, 89, 105));

        panel.add(title, BorderLayout.NORTH);
        panel.add(text, BorderLayout.CENTER);
        return panel;
    }

    private JPanel statCard(JLabel valueLabel, String title, Color accent) {
        JPanel card = new JPanel(new BorderLayout(4, 4));
        card.setBackground(PANEL);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 5, 0, 0, accent),
                BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(223, 230, 240), 1, true),
                        new EmptyBorder(12, 14, 12, 14)
                )
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(new Color(88, 102, 120));
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        valueLabel.setForeground(NAVY);
        valueLabel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 28));
        valueLabel.setText("0");

        card.add(valueLabel, BorderLayout.CENTER);
        card.add(titleLabel, BorderLayout.SOUTH);
        return card;
    }

    private JButton primaryButton(String text) {
        JButton button = baseButton(text);
        button.setBackground(BLACK_BUTTON);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        return button;
    }

    private JButton secondaryButton(String text) {
        JButton button = baseButton(text);
        button.setBackground(SOFT_BLUE);
        button.setForeground(NAVY);
        button.setBorder(BorderFactory.createLineBorder(new Color(211, 221, 232)));
        return button;
    }

    private JButton successButton(String text) {
        JButton button = baseButton(text);
        button.setBackground(TEAL);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        return button;
    }

    private JButton dangerButton(String text) {
        JButton button = baseButton(text);
        button.setBackground(new Color(255, 232, 232));
        button.setForeground(RED);
        button.setBorder(BorderFactory.createLineBorder(new Color(243, 193, 193)));
        return button;
    }

    private JButton baseButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(12, 14, 12, 14));
        button.setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        button.setOpaque(true);
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        return button;
    }

    private void styleInputField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(Color.WHITE);
        field.setForeground(NAVY);
        field.setCaretColor(NAVY);
        field.setPreferredSize(new Dimension(320, 48));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(151, 168, 189), 2, true),
                new EmptyBorder(10, 12, 10, 12)
        ));
    }

    private void styleTextArea(JTextArea area) {
        area.setBackground(new Color(255, 255, 255));
        area.setForeground(NAVY);
        area.setCaretColor(NAVY);
        area.setBorder(new EmptyBorder(10, 12, 10, 12));
    }

    private JScrollPane createStyledScrollPane(JTextArea area) {
        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(53, 78, 109), 4, true),
                new EmptyBorder(3, 3, 3, 3)
        ));
        scrollPane.getViewport().setBackground(new Color(255, 255, 255));
        scrollPane.setBackground(new Color(255, 255, 255));
        scrollPane.setPreferredSize(new Dimension(320, 220));
        return scrollPane;
    }

    private void showMessage(String message, String title, int type) {
        JOptionPane.showMessageDialog(this, message, title, type);
    }

    private static class ComplaintTableModel extends AbstractTableModel {
        private final String[] columns = {"ID", "Description", "Priority", "Level", "Department", "Status", "High Flag", "Created"};
        private final List<Complaint> complaints = new ArrayList<>();

        public void setComplaints(List<Complaint> newComplaints) {
            complaints.clear();
            complaints.addAll(newComplaints);
            fireTableDataChanged();
        }

        public Complaint getComplaint(int row) {
            return complaints.get(row);
        }

        @Override
        public int getRowCount() {
            return complaints.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Complaint complaint = complaints.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> complaint.getId();
                case 1 -> complaint.getDescription();
                case 2 -> complaint.getPriority();
                case 3 -> complaint.getPriorityLevel();
                case 4 -> complaint.getDepartment();
                case 5 -> complaint.getStatus();
                case 6 -> complaint.isHighPriority() ? 1 : 0;
                case 7 -> complaint.getFormattedCreatedAt();
                default -> "";
            };
        }
    }

    private static class PriorityRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                String level = String.valueOf(table.getValueAt(row, 3));
                if ("HIGH".equals(level)) {
                    component.setBackground(new Color(255, 239, 239));
                } else if ("MEDIUM".equals(level)) {
                    component.setBackground(new Color(255, 247, 226));
                } else {
                    component.setBackground(Color.WHITE);
                }
                component.setForeground(NAVY);
            }
            if (component instanceof JLabel label) {
                label.setOpaque(true);
            }
            return component;
        }
    }

    private static class HeaderRenderer extends DefaultTableCellRenderer {
        public HeaderRenderer() {
            setHorizontalAlignment(JLabel.CENTER);
            setHorizontalTextPosition(JLabel.CENTER);
            setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
            setBackground(new Color(18, 35, 58));
            setForeground(Color.WHITE);
            setOpaque(true);
            setBorder(new LineBorder(new Color(18, 35, 58), 1));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setText(value == null ? "" : value.toString());
            setBackground(new Color(18, 35, 58));
            setForeground(Color.WHITE);
            setOpaque(true);
            return this;
        }
    }

    private static class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setPaint(new GradientPaint(0, 0, NAVY, getWidth(), getHeight(), new Color(61, 104, 164)));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 28, 28);
            g2.setColor(new Color(255, 255, 255, 26));
            g2.fillOval(getWidth() - 180, -20, 220, 220);
            g2.setColor(new Color(GOLD.getRed(), GOLD.getGreen(), GOLD.getBlue(), 70));
            g2.fillOval(getWidth() - 280, 30, 120, 120);
            g2.dispose();
            super.paintComponent(graphics);
        }

        @Override
        public boolean isOpaque() {
            return false;
        }
    }
}
