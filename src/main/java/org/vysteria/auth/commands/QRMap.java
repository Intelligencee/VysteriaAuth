package org.vysteria.auth.commands;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.awt.*;

public class QRMap extends MapRenderer {

    private Image qrCode;

    public QRMap(final Image qrCode) {
        this.qrCode = qrCode;
    }

    public void render(final MapView mapView, final MapCanvas mapCanvas, final Player player) {
        mapCanvas.drawImage(0, 0, this.qrCode);
    }
}
