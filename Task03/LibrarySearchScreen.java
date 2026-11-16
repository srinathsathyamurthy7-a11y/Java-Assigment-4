package Task03;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class LibrarySearchScreen extends JFrame {

    static class Book {
        final String isbn;
        final String title;
        final String author;
        final String category;
        boolean available;

        Book(String isbn, String title, String author, String category, boolean available) {
            this.isbn = isbn;
            this.title = title;
            this.author = author;
            this.category = category;
            this.available = available;
        }
    }

    private final List<Book> catalog = new ArrayList<>();
    private final List<Book> filteredBooks = new ArrayList<>();
    private int currentPage = 0;
    private static final int PAGE_SIZE = 5;

    // UI Components
    private final JTextField searchField = new JTextField(18);
    private final JComboBox<String> categoryChoice = new JComboBox<>(
            new String[] { "All Categories", "Computer Science", "Electronics", "Mathematics", "Literature" });
    private final JButton searchButton = new JButton("Search");

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[] { "ISBN", "Title", "Author", "Category", "Status" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable resultsTable = new JTable(tableModel);

    private final JButton prevButton = new JButton("<< Prev");
    private final JButton nextButton = new JButton("Next >>");
    private final JButton reserveButton = new JButton("Reserve Selected Book");
    private final JLabel statusLabel = new JLabel("Showing all catalog books.");

    public LibrarySearchScreen() {
        super("Campus Library Search Screen");
        seedCatalog();

        // Outer Frame uses BorderLayout so CENTER expands freely on resize while NORTH
        // and SOUTH keep preferred height
        setLayout(new BorderLayout(8, 8));

        // 1. NORTH Container: Title Bar + Search Row stacked vertically using
        // BorderLayout
        JPanel topContainer = new JPanel(new BorderLayout());

        JLabel titleBanner = new JLabel("SIMATS CENTRAL LIBRARY - CATALOG SEARCH", SwingConstants.CENTER);
        titleBanner.setFont(new Font("SansSerif", Font.BOLD, 15));
        titleBanner.setOpaque(true);
        titleBanner.setBackground(new Color(25, 65, 120));
        titleBanner.setForeground(Color.WHITE);
        titleBanner.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Search Row uses FlowLayout so components keep their natural preferred height
        // & width
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        searchRow.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        searchRow.add(new JLabel("Keyword:"));
        searchRow.add(searchField);
        searchRow.add(new JLabel("Category:"));
        searchRow.add(categoryChoice);
        searchRow.add(searchButton);

        topContainer.add(titleBanner, BorderLayout.NORTH);
        topContainer.add(searchRow, BorderLayout.SOUTH);

        // 2. CENTER Container: Scrollable Results Area (grows in both width & height
        // when resized)
        resultsTable.setRowHeight(24);
        resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane centerScrollPane = new JScrollPane(resultsTable);
        centerScrollPane.setBorder(BorderFactory.createTitledBorder("Search Results"));

        // 3. SOUTH Container: Action Buttons Row (FlowLayout) + Status Bar
        // (BorderLayout)
        JPanel bottomContainer = new JPanel(new BorderLayout());

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));
        buttonRow.add(prevButton);
        buttonRow.add(nextButton);
        buttonRow.add(reserveButton);

        statusLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(5, 12, 6, 12)));
        statusLabel.setForeground(new Color(0, 90, 150));

        bottomContainer.add(buttonRow, BorderLayout.NORTH);
        bottomContainer.add(statusLabel, BorderLayout.SOUTH);

        add(topContainer, BorderLayout.NORTH);
        add(centerScrollPane, BorderLayout.CENTER);
        add(bottomContainer, BorderLayout.SOUTH);

        // Event Listeners
        searchButton.addActionListener(e -> performSearch());
        searchField.addActionListener(e -> performSearch());
        categoryChoice.addActionListener(e -> performSearch());

        prevButton.addActionListener(e -> {
            if (currentPage > 0) {
                currentPage--;
                refreshTablePage();
            }
        });

        nextButton.addActionListener(e -> {
            if ((currentPage + 1) * PAGE_SIZE < filteredBooks.size()) {
                currentPage++;
                refreshTablePage();
            }
        });

        reserveButton.addActionListener(e -> reserveSelectedBook());

        // Initial load
        performSearch();

        setMinimumSize(new Dimension(560, 340));
        setSize(760, 460);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void seedCatalog() {
        catalog.add(new Book("978-0134685991", "Effective Java", "Joshua Bloch", "Computer Science", true));
        catalog.add(
                new Book("978-0262033848", "Introduction to Algorithms", "Thomas H. Cormen", "Computer Science", true));
        catalog.add(new Book("978-0132350884", "Clean Code", "Robert C. Martin", "Computer Science", false));
        catalog.add(new Book("978-0131103627", "The C Programming Language", "Kernighan & Ritchie", "Computer Science",
                true));
        catalog.add(new Book("978-0073380711", "Microelectronic Circuits", "Sedra & Smith", "Electronics", true));
        catalog.add(new Book("978-0132576277", "Digital Design", "M. Morris Mano", "Electronics", true));
        catalog.add(new Book("978-0321982384", "Linear Algebra and Its Applications", "Gilbert Strang", "Mathematics",
                true));
        catalog.add(new Book("978-0486457925", "Discrete Mathematics", "Norman L. Biggs", "Mathematics", false));
        catalog.add(new Book("978-0140449136", "Crime and Punishment", "Fyodor Dostoevsky", "Literature", true));
    }

    private void performSearch() {
        String keyword = searchField.getText().trim().toLowerCase();
        String selectedCategory = (String) categoryChoice.getSelectedItem();

        filteredBooks.clear();
        for (Book b : catalog) {
            boolean matchesCategory = "All Categories".equals(selectedCategory) || b.category.equals(selectedCategory);
            boolean matchesKeyword = keyword.isEmpty()
                    || b.title.toLowerCase().contains(keyword)
                    || b.author.toLowerCase().contains(keyword)
                    || b.isbn.toLowerCase().contains(keyword);
            if (matchesCategory && matchesKeyword) {
                filteredBooks.add(b);
            }
        }
        currentPage = 0;
        refreshTablePage();
    }

    private void refreshTablePage() {
        tableModel.setRowCount(0);
        int start = currentPage * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, filteredBooks.size());

        for (int i = start; i < end; i++) {
            Book b = filteredBooks.get(i);
            tableModel.addRow(new Object[] {
                    b.isbn, b.title, b.author, b.category, b.available ? "Available" : "Reserved"
            });
        }

        int totalPages = Math.max(1, (int) Math.ceil((double) filteredBooks.size() / PAGE_SIZE));
        prevButton.setEnabled(currentPage > 0);
        nextButton.setEnabled(end < filteredBooks.size());
        statusLabel.setText(String.format("Found %d book(s) | Page %d of %d | Window size: %dx%d",
                filteredBooks.size(), currentPage + 1, totalPages, getWidth(), getHeight()));
    }

    private void reserveSelectedBook() {
        int selectedRow = resultsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a book row from the results table first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Book book = filteredBooks.get(currentPage * PAGE_SIZE + selectedRow);
        if (!book.available) {
            JOptionPane.showMessageDialog(this, "'" + book.title + "' is already reserved.",
                    "Unavailable", JOptionPane.ERROR_MESSAGE);
            return;
        }
        book.available = false;
        refreshTablePage();
        statusLabel.setText("Reserved: '" + book.title + "' (" + book.isbn + ")");
        JOptionPane.showMessageDialog(this, "Successfully reserved '" + book.title + "'!",
                "Reservation Confirmed", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LibrarySearchScreen().setVisible(true));
    }
}