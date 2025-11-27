import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicInteger;

public class ImageProcessingGUI {

    private JFrame frame;
    private CardLayout layout;
    private JPanel main;

    private final String PAGE_HOME = "HOME";
    private final String PAGE_OP = "OP";
    private final String PAGE_PROCESS = "PROCESS";
    private final String PAGE_ENCRYPT = "ENCRYPT";
    private final String PAGE_SAVED = "SAVED";
    private final String PAGE_HISTO = "HISTO";
    private final String PAGE_TRANS = "TRANSFORM";
    private final String PAGE_EDGE  = "EDGE";

    // Images
    private BufferedImage originalImage = null;
    private BufferedImage baseImage = null;
    private BufferedImage previewImage = null;

    // Sliders
    private JSlider contrastSlider, brightnessSlider, valueSlider;
    private JPanel contrastPanel, brightnessPanel, valuePanel;

    // Preview
    private JLabel previewLabel;

    // Operations
    private Operation currentOp = Operation.NONE;
    enum Operation { NONE, ENHANCE, SHARPEN, BLUR, COMPRESS }

    // Counters
    private AtomicInteger enhancedC = new AtomicInteger(1);
    private AtomicInteger sharpenedC = new AtomicInteger(1);
    private AtomicInteger blurredC = new AtomicInteger(1);
    private AtomicInteger compressedC = new AtomicInteger(1);
    private AtomicInteger encryptedC = new AtomicInteger(1);
    private AtomicInteger decryptedC = new AtomicInteger(1);

    // New counters for added features
    private AtomicInteger histC = new AtomicInteger(1);
    private AtomicInteger transC = new AtomicInteger(1);
    private AtomicInteger edgeC = new AtomicInteger(1);

    private static final String SAVED_DIR = "saved";

    // Saved-panel reference
    private JPanel savedPanel;

    public ImageProcessingGUI() throws Exception {
        new AESEncryption();
        Files.createDirectories(new File(SAVED_DIR).toPath());
        buildUI();
    }

    private void buildUI() {
        frame = new JFrame("Modern Image Processing UI");
        frame.setSize(1000, 680);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        layout = new CardLayout();
        main = new JPanel(layout);
        main.setOpaque(false);

        // add pages
        main.add(buildHomePage(), PAGE_HOME);
        main.add(buildOperationsPage(), PAGE_OP);
        main.add(buildProcessPage(), PAGE_PROCESS);
        main.add(buildEncryptionPage(), PAGE_ENCRYPT);
        main.add(buildSavedPage(), PAGE_SAVED);
        main.add(buildHistogramPage(), PAGE_HISTO);
        main.add(buildTransformPage(), PAGE_TRANS);
        main.add(buildEdgePage(), PAGE_EDGE);

        JPanel wrapper = new RoundedPanel(main);
        frame.setContentPane(new GradientPanel());
        frame.add(wrapper);

        frame.setVisible(true);
    }

    // ---------- HOME PAGE ----------
    private JPanel buildHomePage() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(40,40,40,40));

        JLabel title = new JLabel("Image Processing Studio", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(40, 40, 40));
        p.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        RoundedButton upload = new RoundedButton("Upload Image", 20);
        RoundedButton next = new RoundedButton("Go to Operations", 18);
        RoundedButton exit = new RoundedButton("Exit", 16);

        upload.setAlignmentX(Component.CENTER_ALIGNMENT);
        next.setAlignmentX(Component.CENTER_ALIGNMENT);
        exit.setAlignmentX(Component.CENTER_ALIGNMENT);

        upload.addActionListener(e -> uploadImage());
        next.addActionListener(e -> {
            if (originalImage == null) {
                JOptionPane.showMessageDialog(frame,"Upload an image first.");
                return;
            }
            layout.show(main, PAGE_OP);
        });
        exit.addActionListener(e -> frame.dispose());

        center.add(Box.createVerticalGlue());
        center.add(upload);
        center.add(Box.createRigidArea(new Dimension(0, 20)));
        center.add(next);
        center.add(Box.createRigidArea(new Dimension(0, 20)));
        center.add(exit);
        center.add(Box.createVerticalGlue());

        p.add(center, BorderLayout.CENTER);
        return p;
    }

    private void uploadImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select an image");

        int result = chooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                File file = chooser.getSelectedFile();
                originalImage = ImageUtils.loadImage(file);
                if (originalImage == null) {
                    JOptionPane.showMessageDialog(frame, "Invalid image file.");
                    return;
                }
                JOptionPane.showMessageDialog(frame, "Image loaded successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Failed to load image: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    // ---------- OPERATIONS PAGE ----------
    private JPanel buildOperationsPage() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(20,20,20,20));

        JLabel title = new JLabel("Select Operation", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        p.add(title, BorderLayout.NORTH);

        // 3 × 3 grid
        JPanel grid = new JPanel(new GridLayout(3, 3, 20, 20));
        grid.setOpaque(false);

        // Buttons
        RoundedButton enhance      = new RoundedButton("Enhance");
        RoundedButton sharpen      = new RoundedButton("Sharpen");
        RoundedButton blur         = new RoundedButton("Blur");
        RoundedButton compress     = new RoundedButton("Compress");
        RoundedButton histogramBtn = new RoundedButton("Histogram Tools");
        RoundedButton transformBtn = new RoundedButton("Transform Tools");
        RoundedButton edgeBtn      = new RoundedButton("Edge Detection");
        RoundedButton encrypt      = new RoundedButton("Encrypt / Decrypt");
        RoundedButton saved        = new RoundedButton("Saved Items");

        // Actions
        enhance.addActionListener(e -> startOperation(Operation.ENHANCE));
        sharpen.addActionListener(e -> startOperation(Operation.SHARPEN));
        blur.addActionListener(e -> startOperation(Operation.BLUR));
        compress.addActionListener(e -> startOperation(Operation.COMPRESS));

        histogramBtn.addActionListener(e -> {
            if (originalImage == null) {
                JOptionPane.showMessageDialog(frame,"Upload an image first.");
                return;
            }
            layout.show(main, PAGE_HISTO);
        });

        transformBtn.addActionListener(e -> {
            if (originalImage == null) {
                JOptionPane.showMessageDialog(frame,"Upload an image first.");
                return;
            }
            layout.show(main, PAGE_TRANS);
        });

        edgeBtn.addActionListener(e -> {
            if (originalImage == null) {
                JOptionPane.showMessageDialog(frame,"Upload an image first.");
                return;
            }
            layout.show(main, PAGE_EDGE);
        });

        encrypt.addActionListener(e -> layout.show(main, PAGE_ENCRYPT));
        saved.addActionListener(e -> {
            reloadSavedPage();
            layout.show(main, PAGE_SAVED);
        });

        // Add buttons in the correct order (3×3)
        grid.add(enhance);
        grid.add(sharpen);
        grid.add(blur);

        grid.add(compress);
        grid.add(histogramBtn);
        grid.add(transformBtn);

        grid.add(edgeBtn);
        grid.add(encrypt);
        grid.add(saved);

        // Back button
        RoundedButton back = new RoundedButton("Back");
        back.addActionListener(e -> layout.show(main, PAGE_HOME));

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        bottom.add(back);

        p.add(grid, BorderLayout.CENTER);
        p.add(bottom, BorderLayout.SOUTH);

        return p;
    }


    // ---------- PROCESS PAGE ----------
    private JPanel buildProcessPage() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        previewLabel = new JLabel();
        previewLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JScrollPane scroll = new JScrollPane(previewLabel);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        p.add(scroll, BorderLayout.CENTER);

        JPanel sliderPanel = new JPanel();
        sliderPanel.setOpaque(false);
        sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.Y_AXIS));
        sliderPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        contrastSlider = new JSlider(50, 200, 100);
        brightnessSlider = new JSlider(-100, 100, 0);
        valueSlider = new JSlider(0, 100, 50);

        contrastPanel = sliderPane("Contrast", contrastSlider);
        brightnessPanel = sliderPane("Brightness", brightnessSlider);
        valuePanel = sliderPane("Value", valueSlider);

        sliderPanel.add(contrastPanel);
        sliderPanel.add(brightnessPanel);
        sliderPanel.add(valuePanel);

        p.add(sliderPanel, BorderLayout.NORTH);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonRow.setOpaque(false);

        RoundedButton apply = new RoundedButton("Apply");
        RoundedButton save = new RoundedButton("Save Copy");
        RoundedButton discard = new RoundedButton("Discard");
        RoundedButton back = new RoundedButton("Back");

        buttonRow.add(apply);
        buttonRow.add(save);
        buttonRow.add(discard);
        buttonRow.add(back);

        apply.addActionListener(e -> {
            if (previewImage != null) {
                baseImage = ImageUtils.deepCopy(previewImage);
                JOptionPane.showMessageDialog(frame, "Applied (original unchanged)");
            }
        });

        save.addActionListener(e -> saveOperationImage());
        discard.addActionListener(e -> layout.show(main, PAGE_OP));
        back.addActionListener(e -> layout.show(main, PAGE_OP));

        p.add(buttonRow, BorderLayout.SOUTH);

        ChangeListener real = e -> updatePreviewLive();
        contrastSlider.addChangeListener(real);
        brightnessSlider.addChangeListener(real);
        valueSlider.addChangeListener(real);

        return p;
    }

    private JPanel buildEncryptionPage() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(20,20,20,20));

        JLabel title = new JLabel("Encryption / Decryption", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        p.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        RoundedButton encBtn  = new RoundedButton("Encrypt Image");
        RoundedButton decBtn  = new RoundedButton("Decrypt File");
        RoundedButton back    = new RoundedButton("Back");

        JLabel status = new JLabel("Status:", SwingConstants.CENTER);

        // NEW: Encryption pop-up with key + copy button
        encBtn.addActionListener(e -> encryptImage(status));

        // NEW: Decryption asks for key + asks for file
        decBtn.addActionListener(e -> decryptImage(status));

        back.addActionListener(e -> layout.show(main, PAGE_OP));

        center.add(encBtn);
        center.add(Box.createVerticalStrut(15));
        center.add(decBtn);
        center.add(Box.createVerticalStrut(25));
        center.add(status);
        center.add(Box.createVerticalStrut(25));
        center.add(back);

        p.add(center, BorderLayout.CENTER);
        return p;
    }

    // ---------- SAVED ITEMS PAGE ----------
    private JPanel buildSavedPage() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(20,20,20,20));

        JLabel title = new JLabel("Saved Items", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));

        savedPanel = new JPanel(new WrapLayout(FlowLayout.LEFT));
        savedPanel.setOpaque(false);

        JScrollPane sp = new JScrollPane(savedPanel);
        sp.setBorder(null);

        RoundedButton back = new RoundedButton("Back");
        back.addActionListener(e -> layout.show(main, PAGE_OP));

        p.add(title, BorderLayout.NORTH);
        p.add(sp, BorderLayout.CENTER);
        p.add(back, BorderLayout.SOUTH);

        return p;
    }

    private void reloadSavedPage() {
        savedPanel.removeAll();

        File dir = new File(SAVED_DIR);
        File[] list = dir.listFiles();
        if (list == null) return;

        for (File f : list) {
            JPanel tile = new JPanel(new BorderLayout());
            tile.setOpaque(true);
            tile.setBackground(Color.WHITE);
            tile.setBorder(new EmptyBorder(6,6,6,6));
            tile.setPreferredSize(new Dimension(160,120));

            JLabel name = new JLabel(f.getName(), SwingConstants.CENTER);
            name.setFont(new Font("Segoe UI", Font.PLAIN, 11));

            if (f.getName().endsWith(".enc")) {
                tile.add(new JLabel(UIManager.getIcon("FileView.fileIcon"), JLabel.CENTER), BorderLayout.CENTER);
            } else {
                try {
                    BufferedImage img = ImageUtils.loadImage(f);
                    ImageIcon ic = new ImageIcon(ImageUtils.getScaledCopy(img,140,78));
                    JLabel thumb = new JLabel(ic, JLabel.CENTER);
                    tile.add(thumb, BorderLayout.CENTER);

                    // clickable preview
                    tile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    tile.addMouseListener(new MouseAdapter() {
                        public void mouseClicked(MouseEvent e) {
                            openSavedPreview(f);
                        }
                    });

                } catch (Exception ex) { ex.printStackTrace(); }
            }

            tile.add(name, BorderLayout.SOUTH);

            savedPanel.add(tile);
        }

        savedPanel.revalidate();
        savedPanel.repaint();
    }

    private void openSavedPreview(File file) {
        JDialog d = new JDialog(frame, "Preview: " + file.getName(), true);
        d.setSize(600, 500);
        d.setLocationRelativeTo(frame);

        JLabel lbl = new JLabel("", SwingConstants.CENTER);
        try {
            if (file.getName().endsWith(".enc")) {
                lbl.setText("Encrypted file - cannot preview");
            } else {
                BufferedImage img = ImageUtils.loadImage(file);
                lbl.setIcon(new ImageIcon(ImageUtils.getScaledCopy(img, 560, 420)));
            }
        } catch (Exception ex) {
            lbl.setText("Failed to load");
        }

        d.add(lbl);
        d.setVisible(true);
    }

    // ---------- HISTOGRAM PAGE ----------
    private JPanel buildHistogramPage() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12,12,12,12));

        JLabel title = new JLabel("Histogram Tools", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        p.add(title, BorderLayout.NORTH);

        // preview + buttons layout (Style B)
        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);

        JLabel info = new JLabel("Runs on original image. Preview result then Save.", SwingConstants.CENTER);
        info.setBorder(new EmptyBorder(6,6,6,6));
        center.add(info, BorderLayout.NORTH);

        JLabel histPreview = new JLabel();
        histPreview.setHorizontalAlignment(SwingConstants.CENTER);
        JScrollPane sp = new JScrollPane(histPreview);
        sp.setBorder(null);
        center.add(sp, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        btns.setOpaque(false);
        RoundedButton gen = new RoundedButton("Generate Histogram");
        RoundedButton eq = new RoundedButton("Equalize");
        RoundedButton stretch = new RoundedButton("Stretch");
        RoundedButton save = new RoundedButton("Save Result");
        RoundedButton back = new RoundedButton("Back");

        btns.add(gen); btns.add(eq); btns.add(stretch); btns.add(save); btns.add(back);
        center.add(btns, BorderLayout.SOUTH);

        p.add(center, BorderLayout.CENTER);

        // state holder
        final BufferedImage[] lastResult = new BufferedImage[1];
        final BufferedImage[] lastHist = new BufferedImage[1];

        gen.addActionListener(a -> {
            try {
                lastHist[0] = HistogramProcessor.generateHistogramImage(originalImage);
                histPreview.setIcon(new ImageIcon(ImageUtils.getScaledCopy(lastHist[0], 700, 360)));
                lastResult[0] = null;
            } catch (Exception ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(frame,"Histogram failed: "+ex.getMessage()); }
        });

        eq.addActionListener(a -> {
            try {
                lastResult[0] = HistogramProcessor.equalize(originalImage);
                histPreview.setIcon(new ImageIcon(ImageUtils.getScaledCopy(lastResult[0], 700, 360)));
                lastHist[0] = HistogramProcessor.generateHistogramImage(lastResult[0]);
            } catch (Exception ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(frame,"Equalize failed: "+ex.getMessage()); }
        });

        stretch.addActionListener(a -> {
            try {
                lastResult[0] = HistogramProcessor.stretch(originalImage);
                histPreview.setIcon(new ImageIcon(ImageUtils.getScaledCopy(lastResult[0], 700, 360)));
                lastHist[0] = HistogramProcessor.generateHistogramImage(lastResult[0]);
            } catch (Exception ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(frame,"Stretch failed: "+ex.getMessage()); }
        });

        save.addActionListener(a -> {
            try {
                if (lastResult[0] == null && lastHist[0] == null) { JOptionPane.showMessageDialog(frame,"Nothing to save. Run an operation first."); return; }
                if (lastResult[0] != null) {
                    String name = String.format("h_result_%03d.png", histC.getAndIncrement());
                    File out = new File(SAVED_DIR, name);
                    ImageUtils.saveImage(lastResult[0], out, "png", 1f);
                    JOptionPane.showMessageDialog(frame,"Saved: "+name);
                }
                if (lastHist[0] != null) {
                    String hname = String.format("h_hist_%03d.png", histC.getAndIncrement());
                    File hout = new File(SAVED_DIR, hname);
                    ImageUtils.saveImage(lastHist[0], hout, "png", 1f);
                }
            } catch (Exception ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(frame,"Save failed: "+ex.getMessage()); }
        });

        back.addActionListener(a -> layout.show(main, PAGE_OP));

        return p;
    }

    // ---------- TRANSFORM PAGE ----------
    private JPanel buildTransformPage() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12,12,12,12));

        JLabel title = new JLabel("Transform Tools (DFT / DCT / DET)", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        p.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);

        JLabel info = new JLabel("Preview of transform magnitudes (downsampled). Save result.", SwingConstants.CENTER);
        info.setBorder(new EmptyBorder(6,6,6,6));
        center.add(info, BorderLayout.NORTH);

        JLabel transPreview = new JLabel();
        transPreview.setHorizontalAlignment(SwingConstants.CENTER);
        JScrollPane sp = new JScrollPane(transPreview);
        sp.setBorder(null);
        center.add(sp, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        btns.setOpaque(false);
        RoundedButton dft = new RoundedButton("DFT (magnitude)");
        RoundedButton dct = new RoundedButton("DCT (magnitude)");
        RoundedButton det = new RoundedButton("DET (3x3)");
        RoundedButton save = new RoundedButton("Save Result");
        RoundedButton back = new RoundedButton("Back");

        btns.add(dft); btns.add(dct); btns.add(det); btns.add(save); btns.add(back);
        center.add(btns, BorderLayout.SOUTH);
        p.add(center, BorderLayout.CENTER);

        final BufferedImage[] last = new BufferedImage[1];

        dft.addActionListener(a -> {
            try {
                last[0] = TransformProcessor.dftMagnitude(originalImage);
                transPreview.setIcon(new ImageIcon(ImageUtils.getScaledCopy(last[0], 700, 360)));
            } catch (Exception ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(frame,"DFT failed"); }
        });

        dct.addActionListener(a -> {
            try {
                last[0] = TransformProcessor.dctMagnitude(originalImage);
                transPreview.setIcon(new ImageIcon(ImageUtils.getScaledCopy(last[0], 700, 360)));
            } catch (Exception ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(frame,"DCT failed"); }
        });

        det.addActionListener(a -> {
            try {
                last[0] = TransformProcessor.determinantResponse(originalImage);
                transPreview.setIcon(new ImageIcon(ImageUtils.getScaledCopy(last[0], 700, 360)));
            } catch (Exception ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(frame,"DET failed"); }
        });

        save.addActionListener(a -> {
            try {
                if (last[0] == null) { JOptionPane.showMessageDialog(frame,"Run an operation first."); return; }
                String name = String.format("t_result_%03d.png", transC.getAndIncrement());
                File out = new File(SAVED_DIR, name);
                ImageUtils.saveImage(last[0], out, "png", 1f);
                JOptionPane.showMessageDialog(frame, "Saved: " + name);
            } catch (Exception ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(frame,"Save failed"); }
        });

        back.addActionListener(a -> layout.show(main, PAGE_OP));

        return p;
    }

    // ---------- EDGE DETECTION PAGE ----------
    private JPanel buildEdgePage() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12,12,12,12));

        JLabel title = new JLabel("Edge Detection", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        p.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);

        JLabel info = new JLabel("Apply edge detection to original image. Preview & save.", SwingConstants.CENTER);
        info.setBorder(new EmptyBorder(6,6,6,6));
        center.add(info, BorderLayout.NORTH);

        JLabel edgePreview = new JLabel();
        edgePreview.setHorizontalAlignment(SwingConstants.CENTER);
        JScrollPane sp = new JScrollPane(edgePreview);
        sp.setBorder(null);
        center.add(sp, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        btns.setOpaque(false);
        RoundedButton sobel = new RoundedButton("Sobel");
        RoundedButton canny = new RoundedButton("Canny (approx)");
        RoundedButton save = new RoundedButton("Save Result");
        RoundedButton back = new RoundedButton("Back");

        btns.add(sobel); btns.add(canny); btns.add(save); btns.add(back);
        center.add(btns, BorderLayout.SOUTH);
        p.add(center, BorderLayout.CENTER);

        final BufferedImage[] last = new BufferedImage[1];

        sobel.addActionListener(a -> {
            try {
                last[0] = EdgeDetector.sobel(originalImage);
                edgePreview.setIcon(new ImageIcon(ImageUtils.getScaledCopy(last[0], 700, 360)));
            } catch (Exception ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(frame,"Sobel failed"); }
        });

        canny.addActionListener(a -> {
            try {
                last[0] = EdgeDetector.cannyApprox(originalImage);
                edgePreview.setIcon(new ImageIcon(ImageUtils.getScaledCopy(last[0], 700, 360)));
            } catch (Exception ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(frame,"Canny failed"); }
        });

        save.addActionListener(a -> {
            try {
                if (last[0] == null) { JOptionPane.showMessageDialog(frame,"Run an operation first."); return; }
                String name = String.format("edge_%03d.png", edgeC.getAndIncrement());
                File out = new File(SAVED_DIR, name);
                ImageUtils.saveImage(last[0], out, "png", 1f);
                JOptionPane.showMessageDialog(frame, "Saved: " + name);
            } catch (Exception ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(frame,"Save failed"); }
        });

        back.addActionListener(a -> layout.show(main, PAGE_OP));
        return p;
    }

    // ---------- PROCESS OPERATIONS (existing) ----------
    private void startOperation(Operation op) {
        if (originalImage == null) {
            JOptionPane.showMessageDialog(frame,"Upload first.");
            return;
        }
        currentOp = op;

        contrastPanel.setVisible(false);
        brightnessPanel.setVisible(false);
        valuePanel.setVisible(false);

        baseImage = ImageUtils.deepCopy(originalImage);
        previewImage = ImageUtils.deepCopy(originalImage);

        switch(op){
            case ENHANCE:
                contrastPanel.setVisible(true);
                brightnessPanel.setVisible(true);
                break;
            case SHARPEN:
                valuePanel.setVisible(true);
                ((JLabel)valuePanel.getComponent(0)).setText("Sharpness");
                valueSlider.setMinimum(50);
                valueSlider.setMaximum(300);
                valueSlider.setValue(100);
                break;
            case BLUR:
                valuePanel.setVisible(true);
                ((JLabel)valuePanel.getComponent(0)).setText("Blur Radius");
                valueSlider.setMinimum(0);
                valueSlider.setMaximum(50);
                valueSlider.setValue(0);
                break;
            case COMPRESS:
                valuePanel.setVisible(true);
                ((JLabel)valuePanel.getComponent(0)).setText("JPEG Quality");
                valueSlider.setMinimum(10);
                valueSlider.setMaximum(100);
                valueSlider.setValue(80);
                break;
            default:
                break;
        }

        updatePreview();
        layout.show(main, PAGE_PROCESS);
    }

    private void updatePreview(){
        previewLabel.setText(null);
        previewLabel.setIcon(new ImageIcon(ImageUtils.getScaledCopy(previewImage, 820, 500)));
    }

    private void updatePreviewLive() {
        if (currentOp == Operation.NONE || baseImage == null) return;

        new SwingWorker<BufferedImage,Void>() {
            protected BufferedImage doInBackground() {
                BufferedImage temp = ImageUtils.deepCopy(baseImage);
                try {
                    switch(currentOp){
                        case ENHANCE:
                            float c = contrastSlider.getValue() / 100f;
                            int b = brightnessSlider.getValue();
                            temp = ImageEnhancer.enhance(temp, c, b);
                            break;
                        case SHARPEN:
                            float s = valueSlider.getValue() / 100f;
                            temp = ImageSharpener.sharpen(temp, s);
                            break;
                        case BLUR:
                            int r = valueSlider.getValue();
                            temp = ImageBlurrer.blurRadius(temp, r);
                            break;
                        case COMPRESS:
                            int q = valueSlider.getValue();
                            File out = new File("temp.jpg");
                            ImageCompressor.saveCompressedJPG(temp, out, q / 100f);
                            temp = ImageUtils.loadImage(out);
                            break;
                        default:
                            break;
                    }
                } catch (Exception e){ e.printStackTrace(); }
                return temp;
            }

            protected void done(){
                try{
                    previewImage = get();
                    updatePreview();
                } catch(Exception ignore){}
            }
        }.execute();
    }

    private void saveOperationImage() {
        if (previewImage == null) return;
        try {
            String name;
            File out;

            switch(currentOp){
                case ENHANCE:
                    name = String.format("enhanced_%03d.png", enhancedC.getAndIncrement());
                    out = new File(SAVED_DIR, name);
                    ImageUtils.saveImage(previewImage, out, "png", 1f);
                    break;
                case SHARPEN:
                    name = String.format("sharpened_%03d.png", sharpenedC.getAndIncrement());
                    out = new File(SAVED_DIR, name);
                    ImageUtils.saveImage(previewImage, out, "png", 1f);
                    break;
                case BLUR:
                    name = String.format("blurred_%03d.png", blurredC.getAndIncrement());
                    out = new File(SAVED_DIR, name);
                    ImageUtils.saveImage(previewImage, out, "png", 1f);
                    break;
                case COMPRESS:
                    name = String.format("compressed_%03d.jpg", compressedC.getAndIncrement());
                    out = new File(SAVED_DIR, name);
                    ImageCompressor.saveCompressedJPG(previewImage, out, valueSlider.getValue()/100f);
                    break;
                default:
                    return;
            }

            JOptionPane.showMessageDialog(frame, "Saved: " + out.getName());
        } catch (Exception e){
            JOptionPane.showMessageDialog(frame,"Save failed: "+e.getMessage());
        }
    }

    // ---------- ENCRYPT / DECRYPT (existing) ----------
    private void encryptImage(JLabel status) {
    try {
        if (originalImage == null) {
            JOptionPane.showMessageDialog(frame, "No image loaded!");
            return;
        }

        // Generate a fresh AES instance with a fresh key
        AESEncryption aesTemp = new AESEncryption();
        String keyHex = aesTemp.getKeyHex();  // show to user

        // Convert original image → PNG bytes
        byte[] bytes = ImageUtils.toBytes(originalImage, "png", 1f);

        // Encrypt
        byte[] enc = aesTemp.encrypt(bytes);

        // Save encrypted file
        String name = String.format("encrypted_%03d.enc", encryptedC.getAndIncrement());
        File out = new File(SAVED_DIR, name);
        Files.write(out.toPath(), enc);

        // Popup with key + copy button
        JTextArea ta = new JTextArea(keyHex);
        ta.setEditable(false);

        JButton copyBtn = new JButton("Copy Key");
        copyBtn.addActionListener(e -> {
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new java.awt.datatransfer.StringSelection(keyHex), null);
            JOptionPane.showMessageDialog(frame, "Copied!");
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Image encrypted successfully!\nYour Key:"), BorderLayout.NORTH);
        panel.add(new JScrollPane(ta), BorderLayout.CENTER);
        panel.add(copyBtn, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(frame, panel, "Encryption Successful", JOptionPane.INFORMATION_MESSAGE);

        status.setText("Encrypted and saved: " + name);

    } catch (Exception ex) {
        status.setText("Encryption failed!");
        ex.printStackTrace();
    }
}


    private void decryptImage(JLabel status) {
    // Ask for key
    JTextField keyField = new JTextField();

    JButton fileBtn = new JButton("Choose Encrypted File");
    JLabel fileLabel = new JLabel("No file selected");

    final File[] selected = new File[1];

    fileBtn.addActionListener(e -> {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            selected[0] = fc.getSelectedFile();
            fileLabel.setText(selected[0].getName());
        }
    });

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.add(new JLabel("Enter Key:"));
    panel.add(keyField);
    panel.add(Box.createVerticalStrut(10));
    panel.add(fileBtn);
    panel.add(fileLabel);

    int result = JOptionPane.showConfirmDialog(frame, panel, "Decrypt Image", JOptionPane.OK_CANCEL_OPTION);

    if (result != JOptionPane.OK_OPTION) return;

    if (selected[0] == null) {
        JOptionPane.showMessageDialog(frame, "No encrypted file selected!");
        return;
    }

    try {
        // Recreate AES object from user key
        AESEncryption aesTemp = new AESEncryption(keyField.getText().trim());

        byte[] encrypted = Files.readAllBytes(selected[0].toPath());
        byte[] plain = aesTemp.decrypt(encrypted);

        BufferedImage img = ImageUtils.loadImage(new ByteArrayInputStream(plain));

        String name = String.format("decrypted_%03d.png", decryptedC.getAndIncrement());
        File out = new File(SAVED_DIR, name);
        ImageUtils.saveImage(img, out, "png", 1f);

        JOptionPane.showMessageDialog(frame, "Decryption successful!\nSaved as " + name);

        status.setText("Decrypted and saved!");

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(frame, "Decryption failed!\nInvalid key or file!", "Error", JOptionPane.ERROR_MESSAGE);
        status.setText("Decryption failed");
    }
}


    // ---------- UTILITY COMPONENTS ----------
    private JPanel sliderPane(String label, JSlider slider) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JLabel l = new JLabel(label, SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        slider.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(l);
        p.add(slider);
        p.setBorder(new EmptyBorder(10,10,10,10));
        return p;
    }

    // ---------- STYLE ELEMENTS ----------
    static class GradientPanel extends JPanel {
        protected void paintComponent(Graphics g){
            Graphics2D g2 = (Graphics2D)g;
            g2.setPaint(new GradientPaint(0,0,new Color(245,245,250),
                    getWidth(),getHeight(),new Color(228,239,255)));
            g2.fillRect(0,0,getWidth(),getHeight());
        }
    }

    static class RoundedPanel extends JPanel {
        private JPanel inner;
        public RoundedPanel(JPanel inner){
            this.inner = inner;
            setOpaque(false);
            setLayout(new GridBagLayout());
            add(new Inner());
        }

        class Inner extends JPanel {
            Inner(){
                setLayout(new BorderLayout());
                add(inner);
                setOpaque(false);
            }
            protected void paintComponent(Graphics g){
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Shape r = new RoundRectangle2D.Float(0,0,getWidth()-1,getHeight()-1,25,25);
                g2.setColor(new Color(255,255,255,235));
                g2.fill(r);
                g2.setColor(new Color(200,200,210));
                g2.draw(r);
                g2.dispose();
            }
        }
    }

    static class RoundedButton extends JButton {
        private int radius = 16;
        private Color base = new Color(66,133,244);
        private Color hover = new Color(54,114,204);

        RoundedButton(String text){
            this(text,14);
        }

        RoundedButton(String text, int size){
            super(text);
            setFont(new Font("Segoe UI", Font.BOLD, size));
            setFocusPainted(false);
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter(){
                public void mouseEntered(MouseEvent e){ setBackground(hover); repaint(); }
                public void mouseExited(MouseEvent e){ setBackground(base); repaint(); }
            });
        }

        protected void paintComponent(Graphics g){
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(getBackground().equals(hover) ? hover : base);
            g2.fillRoundRect(0,0,getWidth(),getHeight(), radius, radius);

            g2.setColor(Color.WHITE);
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText()))/2;
            int y = (getHeight() - fm.getHeight())/2 + fm.getAscent();
            g2.drawString(getText(),x,y);

            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new ImageProcessingGUI();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
