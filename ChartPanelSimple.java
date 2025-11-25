package ui;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class ChartPanelSimple extends JPanel {
    private Map<String,Integer> data;
    public ChartPanelSimple(Map<String,Integer> data) { this.data = data; }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (data == null || data.isEmpty()) {
            g.drawString("No data", 10, 20);
            return;
        }
        Graphics2D g2 = (Graphics2D) g;
        int width = getWidth() - 40;
        int height = getHeight() - 40;
        int x = 20, y = 20;
        int barCount = data.size();
        int gap = 10;
        int barWidth = Math.max(20, (width - (barCount-1)*gap)/barCount);
        int max = data.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        int i=0;
        for (Map.Entry<String,Integer> e : data.entrySet()) {
            int val = e.getValue();
            int barHeight = (int)((double)val / max * (height - 40));
            int bx = x + i*(barWidth+gap);
            int by = y + (height - barHeight);
            g2.setColor(new Color(100,150,200));
            g2.fillRect(bx, by, barWidth, barHeight);
            g2.setColor(Color.BLACK);
            g2.drawRect(bx, by, barWidth, barHeight);
            String label = e.getKey();
            g2.drawString(label, bx, y+height+15);
            g2.drawString(String.valueOf(val), bx, by-5);
            i++;
        }
    }
}
