package gui;

import entity.Course;
import entity.Session;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimetableView extends JPanel implements PropertyChangeListener {
    private TimetableViewModel timetableViewModel;

    // UI components
    private JTable timetableTable;
    private JLabel totalDistanceLabel;

    public TimetableView(TimetableViewModel timetableViewModel) {
        this.timetableViewModel = timetableViewModel;
        initializeUI();
        timetableViewModel.addPropertyChangeListener(this);
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // Initialize the table model
        TimetableTableModel model = new TimetableTableModel();

        // Timetable table setup
        timetableTable = new JTable(model);
        timetableTable.setFillsViewportHeight(true);

        // Set the custom renderer with the data model
        timetableTable.setDefaultRenderer(Object.class, new CourseCellRenderer(model.getData()));
        // After initializing the timetableTable
        timetableTable.setRowHeight(60); // Choose an appropriate height
        timetableTable.setShowGrid(false);
        timetableTable.setIntercellSpacing(new Dimension(0, 0)); // This will reduce the space between cells



        // Add the table to a scroll pane and then to the panel
        add(new JScrollPane(timetableTable), BorderLayout.CENTER);

        // Total distance label setup
        totalDistanceLabel = new JLabel("Total Distance: 0 km");
        add(totalDistanceLabel, BorderLayout.SOUTH);
    }




    // Inner class for the timetable table model
    class TimetableTableModel extends AbstractTableModel {
        private final String[] columnNames = {"Time", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        private Object[][] data; // Fill with course data

        public TimetableTableModel() {
            // Initialize with empty data array with the right size (e.g., 24 slots for a 12-hour day with 30-minute intervals)
            data = new Object[13][6]; // Adjust to match the number of time slots you have
            LocalTime time = LocalTime.of(8, 0); // Adjust start time as needed
            for (int i = 0; i < data.length; i++) {
                data[i][0] = time.format(DateTimeFormatter.ofPattern("HH:mm"));
                time = time.plusHours(1);// Adjust interval as needed
            }
        }
        public Object[][] getData() {
            return data;
        }

        @Override
        public int getRowCount() {
            return data.length;
        }


        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public Object getValueAt(int row, int col) {
            return data[row][col];
        }

        public void setValueAt(Object value, int row, int col) {
            data[row][col] = value;
            fireTableCellUpdated(row, col);
        }

        public void setData(Object[][] newData) {
            data = newData;
            fireTableDataChanged();
            // Update the renderer to use the new data array
            if (timetableTable != null) {
                timetableTable.setDefaultRenderer(Object.class, new CourseCellRenderer(data));
            }
        }
    }

    // This method needs to be filled out with logic specific to your application
    private void updateTimetableDisplay(List<Course> courses) {
        // Define the start and end times of your schedule
        LocalTime scheduleStart = LocalTime.of(8, 0); // Example: 8 AM
        LocalTime scheduleEnd = LocalTime.of(21, 0); // Example: 8 PM

        // Calculate the number of rows needed (e.g., for 30-minute slots in a 12-hour schedule, we need 24 rows)
        int totalSlots = (int) ChronoUnit.MINUTES.between(scheduleStart, scheduleEnd) / 30;
        Object[][] timetableData = new Object[totalSlots][6]; // 5 days + 1 for time column

        // Initialize time column
        LocalTime timeIterator = scheduleStart;
        for (int i = 0; i < totalSlots; i++) {
            timetableData[i][0] = timeIterator.toString();
            timeIterator = timeIterator.plusMinutes(60); // Corrected to 30-minute increment
        }

        // Process courses and their sessions
        for (Course course : courses) {
            // Handle different types of sessions: lectures, tutorials, and practicals
            processSessions(course.getLecSessions(), course.getCourseName(), "LEC", timetableData);
            processSessions(course.getTutSessions(), course.getCourseName(), "TUT", timetableData);
            processSessions(course.getPraSessions(), course.getCourseName(), "PRA", timetableData);
        }

        // Now that we've built the data, update the table model
        TimetableTableModel model = (TimetableTableModel) timetableTable.getModel();
        model.setData(timetableData); // Update the model with new data
    }
    private void processSessions(List<Session> sessions, String courseName, String sessionType, Object[][] timetableData) {
        LocalTime scheduleStart = LocalTime.of(8, 0); // The timetable starts at 8 AM
        int slotDurationInMinutes = 60; // Each slot is 30 minutes
        System.out.println(courseName);


        for (Session session : sessions) {
            List<Integer> startTimes = session.getStartTime(); // Start times in milliseconds
            List<Integer> endTimes = session.getEndTime(); // End times in milliseconds
            List<Integer> days = session.getDay(); // Day indices (1 for Monday, etc.)
            System.out.println(startTimes);
            System.out.println(endTimes);

            for (int i = 0; i < startTimes.size(); i++) {
                // Convert times from milliseconds to 'LocalTime'
                LocalTime startTime = LocalTime.ofSecondOfDay(startTimes.get(i) * 3600);
                LocalTime endTime = LocalTime.ofSecondOfDay(endTimes.get(i) * 3600);

                System.out.println("Start Time: " + startTime);
                System.out.println("End Time: " + endTime);

                // Calculate row indices
                int startRow = (int) ChronoUnit.MINUTES.between(scheduleStart, startTime) / slotDurationInMinutes;
                int endRow = (int) ChronoUnit.MINUTES.between(scheduleStart, endTime) / slotDurationInMinutes;

                System.out.println("Start Row: " + startRow);
                System.out.println("End Row: " + endRow);

                // Get the day column index, adjust if your array doesn't start with index 1 for Monday
                int dayColumn = days.get(i) + 1; // Assuming timetableData[0][1] is Monday

                System.out.println("Day Column: " + dayColumn + " " + "Len: " + timetableData[0].length);

                // Check bounds and fill in the timetableData
                if (dayColumn >= 1 && dayColumn < timetableData[0].length) {

                    for (int row = startRow; row < endRow; row++) {
                        System.out.println("Reached");
                        if (row >= 0 && row < timetableData.length) {
                            timetableData[row][dayColumn] = courseName + " " + session.getSessionCode();
                            System.out.println("Setting [" + row + "][" + dayColumn + "] to " + timetableData[row][dayColumn]);
                        }
                    }
                }
            }
        }
    }



    class CourseCellRenderer extends DefaultTableCellRenderer {
        private final Object[][] timetableData;
        private final Map<String, Color> courseColors; // Map to store course-specific colors
        private final Color lightGray = new Color(220, 220, 220);

    public CourseCellRenderer(Object[][] timetableData) {
            this.timetableData = timetableData;
            this.courseColors = new HashMap<>();
            initializeCourseColors();

        }

        private void initializeCourseColors() {
            // Define a list of colors to use for up to 7 courses
            List<Color> colors = Arrays.asList(
                    new Color(255, 204, 204), // Light red
                    new Color(204, 255, 204), // Light green
                    new Color(204, 204, 255), // Light blue
                    new Color(255, 255, 204), // Light yellow
                    new Color(255, 204, 255), // Light purple
                    new Color(204, 255, 255), // Light cyan
                    new Color(255, 223, 186)  // Light orange
            );

            // Loop through each cell in the timetable data and assign a color to each course
            for (Object[] row : timetableData) {
                for (Object cell : row) {
                    if (cell instanceof String && ((String) cell).matches(".*[a-zA-Z]+.*\\d+.*")) {
                        String courseCode = ((String) cell).split(" ")[0];
                        courseColors.putIfAbsent(courseCode, colors.get(courseColors.size() % colors.size()));
                    }
                }
            }
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(CENTER); // Ensure text is centered horizontally
            setVerticalAlignment(CENTER); // Ensure text is centered vertically

            // Check if the row corresponds to a full hour and set a top border if it does

            boolean isFirstColumn = column == 0; // Assuming first column is the hour column

            // Assign a background color based on the course code
            if (isFirstColumn) {
                setBackground(lightGray);
                setForeground(Color.BLACK);
                setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.BLACK));
            } else {
                // Assign a background color based on the course code for course cells
                if (value instanceof String && ((String) value).matches(".*[a-zA-Z]+.*\\d+.*")) {
                    String courseCode = ((String) value).split(" ")[0];
                    Color color = courseColors.getOrDefault(courseCode, table.getBackground());
                    setBackground(color);
                    setForeground(Color.BLACK);

                    // Hide text and remove borders for non-first cells of merged blocks
                    if (row > 0 && value.equals(timetableData[row - 1][column])) {
                        setText("");
                        setBorder(BorderFactory.createEmptyBorder());
                    } else {
                        setText((String) value);
                        setBorder(BorderFactory.createEmptyBorder());
                    }
                } else {
                    // For cells that are not part of a course
                    setBackground(table.getBackground());
                    setForeground(table.getForeground());
                    setText(value != null ? value.toString() : "");
                    setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, lightGray));
                }
            }

            return this;
        }
    }




    private void updateTotalDistanceDisplay(double totalDistance) {
        totalDistanceLabel.setText("Total Distance: " + totalDistance + " km");
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("courses".equals(evt.getPropertyName())) {
            updateTimetableDisplay((List<Course>) evt.getNewValue());
        } else if ("totalDistance".equals(evt.getPropertyName())) {
            updateTotalDistanceDisplay((Double) evt.getNewValue());
        }
    }
}
