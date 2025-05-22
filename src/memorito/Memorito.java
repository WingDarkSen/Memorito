// Archivo: Memorito.java
// Código final de Memorito - versión consolidada y depurada

package memorito;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.InputStream;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.Timer;
import java.util.List;
import java.util.ArrayList;

public class Memorito {

    static JFrame ventana;
    static JPanel panelJuego;
    static JLabel estado;
    static JLabel cronometroLabel;
    static JButton playBtn, resetBtn, exitBtn;
    static Timer cronometro;
    static int segundos = 0;
    static int tamaño = 0;
    static String modo = null;

    static JButton primerBoton = null;
    static JButton segundoBoton = null;
    static Map<JButton, Integer> valores = new HashMap<>();
    static int parejasEncontradas = 0;
    static int totalParejas = 0;

    static List<ImageIcon> imagenesPersonalizadas = new ArrayList<>();
    static List<ImageIcon> imagenesSugeridas = new ArrayList<>();

    static JMenuItem numeros, letras, myImages;

    public static void main(String[] args) {
        ventana = new JFrame("Memorito");
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setLayout(new BorderLayout());
        cargarImagenesSugeridas();

        JMenuBar barraMenu = new JMenuBar();
        JMenu menuSetGame = new JMenu("Set Game");
        menuSetGame.setToolTipText("Configura el modo de juego (modo y tamaño)");

        JMenuItem board4x4 = new JMenuItem("Board 4 x 4");
        board4x4.setToolTipText("Tablero pequeño para principiantes");
        JMenuItem board6x6 = new JMenuItem("Board 6 x 6");
        board6x6.setToolTipText("Tablero intermedio, para entrenar memoria");

        numeros = new JMenuItem("Play with Numbers");
        letras = new JMenuItem("Play with Letters");
        myImages = new JMenuItem("Play with Images");

        numeros.setToolTipText("Jugar usando números");
        letras.setToolTipText("Jugar usando letras");
        myImages.setToolTipText("Sube tus propias imágenes para jugar");

        numeros.setEnabled(false);
        letras.setEnabled(false);
        myImages.setEnabled(false);

        board4x4.addActionListener(e -> seleccionarTamaño(4));
        board6x6.addActionListener(e -> seleccionarTamaño(6));

        numeros.addActionListener(e -> seleccionarModo("Numbers"));
        letras.addActionListener(e -> seleccionarModo("Letters"));
        myImages.addActionListener(e -> cargarImagenesUsuario());

        menuSetGame.add(board4x4);
        menuSetGame.add(board6x6);
        menuSetGame.addSeparator();
        menuSetGame.add(numeros);
        menuSetGame.add(letras);
        menuSetGame.add(myImages);

        JMenu menuHelp = new JMenu("Help");
        JMenuItem about = new JMenuItem("About");
        JMenuItem manual = new JMenuItem("Manual");

        about.setToolTipText("Acerca del juego");
        manual.setToolTipText("Cómo se juega y reglas básicas");

        about.addActionListener(e -> mostrarAbout());
        manual.addActionListener(e -> mostrarManual());

        menuHelp.add(about);
        menuHelp.add(manual);

        barraMenu.add(menuSetGame);
        barraMenu.add(menuHelp);
        ventana.setJMenuBar(barraMenu);

        JPanel panelSuperior = new JPanel(new BorderLayout());
        estado = new JLabel("Selecciona modo y tamaño");
        estado.setForeground(Color.RED);
        estado.setFont(new Font("Arial", Font.BOLD, 16));

        playBtn = new JButton("Play");
        playBtn.setEnabled(false);
        playBtn.addActionListener(e -> iniciarJuego());

        resetBtn = new JButton("Reset");
        resetBtn.addActionListener(e -> reiniciarTodo());

        exitBtn = new JButton("Exit");
        exitBtn.addActionListener(e -> System.exit(0));

        JPanel botones = new JPanel();
        botones.add(playBtn);
        botones.add(resetBtn);
        botones.add(exitBtn);

        cronometroLabel = new JLabel("Tiempo: 0s");
        cronometroLabel.setHorizontalAlignment(SwingConstants.CENTER);

        panelSuperior.add(estado, BorderLayout.WEST);
        panelSuperior.add(cronometroLabel, BorderLayout.CENTER);
        panelSuperior.add(botones, BorderLayout.EAST);

        panelJuego = new JPanel();
        ventana.add(panelSuperior, BorderLayout.NORTH);
        ventana.add(panelJuego, BorderLayout.CENTER);

        ventana.setSize(800, 800);
        ventana.setLocationRelativeTo(null);
        ventana.setVisible(true);
    }

    static void seleccionarTamaño(int size) {
        tamaño = size;
        numeros.setEnabled(true);
        letras.setEnabled(true);
        myImages.setEnabled(true);
        actualizarEstado();
    }

    static void seleccionarModo(String m) {
        modo = m;
        actualizarEstado();
        habilitarPlay();
    }

    static void iniciarJuego() {
        panelJuego.removeAll();
        panelJuego.setLayout(new GridLayout(tamaño, tamaño));

        int cantidadPares = (tamaño * tamaño) / 2;
        List<Object> contenido = new ArrayList<>();

        if (modo.equals("Numbers")) {
            for (int i = 1; i <= cantidadPares; i++) {
                contenido.add(i); contenido.add(i);
            }
        } else if (modo.equals("Letters")) {
            for (int i = 0; i < cantidadPares; i++) {
                char letra = (char) ('A' + i);
                contenido.add(letra); contenido.add(letra);
            }
        } else if (modo.equals("Images")) {
            List<ImageIcon> finalImgs = new ArrayList<>();
            finalImgs.addAll(imagenesPersonalizadas);
            Collections.shuffle(imagenesSugeridas);
            while (finalImgs.size() < cantidadPares && !imagenesSugeridas.isEmpty()) {
                finalImgs.add(imagenesSugeridas.remove(0));
            }
            for (ImageIcon img : finalImgs) {
                contenido.add(img); contenido.add(img);
            }
        }

        Collections.shuffle(contenido);
        valores.clear();
        primerBoton = segundoBoton = null;
        parejasEncontradas = 0;
        totalParejas = cantidadPares;

        for (Object item : contenido) {
            JButton boton = new JButton("?");
            boton.setFont(new Font("Arial", Font.BOLD, 20));
            if (item instanceof Integer || item instanceof Character) {
                String texto = item.toString();
                valores.put(boton, texto.hashCode());
                boton.addActionListener(e -> manejarTexto(boton, texto));
            } else if (item instanceof ImageIcon img) {
                valores.put(boton, img.hashCode());
                boton.addActionListener(e -> manejarImagen(boton, img));
            }
            panelJuego.add(boton);
        }

        panelJuego.revalidate();
        panelJuego.repaint();
        iniciarCronometro();
    }

    static void manejarTexto(JButton boton, String texto) {
        if (boton.getText().equals("?") && segundoBoton == null) {
            boton.setText(texto);
            if (primerBoton == null) primerBoton = boton;
            else if (primerBoton != boton) {
                segundoBoton = boton;
                Timer timer = new Timer(500, ev -> verificarPareja());
                timer.setRepeats(false);
                timer.start();
            }
        }
    }

    static void manejarImagen(JButton boton, ImageIcon img) {
        if (boton.getText().equals("?") && segundoBoton == null) {
            boton.setIcon(img); boton.setText("");
            if (primerBoton == null) primerBoton = boton;
            else if (primerBoton != boton) {
                segundoBoton = boton;
                Timer timer = new Timer(500, ev -> verificarPareja());
                timer.setRepeats(false);
                timer.start();
            }
        }
    }

    static void verificarPareja() {
        Integer v1 = valores.get(primerBoton);
        Integer v2 = valores.get(segundoBoton);
        if (v1 == null || v2 == null) return;
        if (!v1.equals(v2)) {
            primerBoton.setText("?"); segundoBoton.setText("?");
            primerBoton.setIcon(null); segundoBoton.setIcon(null);
        } else {
            primerBoton.setEnabled(false); segundoBoton.setEnabled(false);
            parejasEncontradas++;
            if (parejasEncontradas == totalParejas) {
                cronometro.stop();
                numeros.setEnabled(false);
                letras.setEnabled(false);
                myImages.setEnabled(false);
                JOptionPane.showMessageDialog(ventana, "✨ Felicitaciones, lo lograste en " + segundos + " segundos!", "Juego terminado", JOptionPane.INFORMATION_MESSAGE);
            }
        }
        primerBoton = segundoBoton = null;
    }

    static void actualizarEstado() {
        if (modo != null && tamaño > 0)
            estado.setText("Current: " + tamaño + "x" + tamaño + " | Mode: " + modo);
        else if (tamaño > 0)
            estado.setText("Current: " + tamaño + "x" + tamaño + " | Mode: not set");
        else
            estado.setText("Selecciona modo y tamaño");
    }

    static void habilitarPlay() {
        playBtn.setEnabled(true);
    }

    static void reiniciarTodo() {
        tamaño = 0;
        modo = null;
        imagenesPersonalizadas.clear();
        estado.setText("Selecciona modo y tamaño");
        playBtn.setEnabled(false);
        numeros.setEnabled(false);
        letras.setEnabled(false);
        myImages.setEnabled(false);
        panelJuego.removeAll();
        panelJuego.revalidate();
        panelJuego.repaint();
    }

    static void iniciarCronometro() {
        segundos = 0;
        cronometro = new Timer(1000, e -> cronometroLabel.setText("Tiempo: " + (++segundos) + "s"));
        cronometro.start();
    }

    static void cargarImagenesSugeridas() {
        imagenesSugeridas.clear();
        int dimension = 100;
        for (int i = 1; i <= 50; i++) {
            try (InputStream is = Memorito.class.getResourceAsStream("/resources/img" + i + ".png")) {
                if (is != null) {
                    Image img = ImageIO.read(is);
                    ImageIcon icon = new ImageIcon(img.getScaledInstance(dimension, dimension, Image.SCALE_SMOOTH));
                    imagenesSugeridas.add(icon);
                }
            } catch (Exception e) {
                System.out.println("No se pudo cargar: img" + i);
            }
        }
    }

    static void cargarImagenesUsuario() {
        if (tamaño == 0) {
            JOptionPane.showMessageDialog(ventana, "Primero selecciona el tamaño del tablero.");
            return;
        }

        JDialog dialogo = new JDialog(ventana, "Warning", true);
        dialogo.setUndecorated(true);
        dialogo.setLayout(new BorderLayout());
        dialogo.setSize(400, 150);
        dialogo.setLocationRelativeTo(ventana);

        JLabel mensaje = new JLabel("¿Qué tipo de imágenes deseas usar?", JLabel.CENTER);
        JPanel botones = new JPanel();

        JButton tipBtn = new JButton("Tip");
        JButton mineBtn = new JButton("Mine");
        JButton cancelBtn = new JButton("Cancel");

        tipBtn.addActionListener(e -> {
            imagenesPersonalizadas.clear();
            int cantidad = tamaño * tamaño / 2;
            Collections.shuffle(imagenesSugeridas);
            if (imagenesSugeridas.size() >= cantidad) {
                imagenesPersonalizadas.addAll(imagenesSugeridas.subList(0, cantidad));
            } else {
                JOptionPane.showMessageDialog(ventana, "No hay suficientes imágenes sugeridas disponibles.", "Error", JOptionPane.ERROR_MESSAGE);
                dialogo.dispose();
                return;
            }
            modo = "Images";
            actualizarEstado();
            habilitarPlay();
            dialogo.dispose();
        });

        mineBtn.addActionListener(e -> {
            imagenesPersonalizadas.clear();
            dialogo.dispose();
            JFileChooser chooser = new JFileChooser();
            chooser.setMultiSelectionEnabled(true);
            if (chooser.showOpenDialog(ventana) == JFileChooser.APPROVE_OPTION) {
                int dimension = (tamaño == 4) ? 128 : 100;
                for (File archivo : chooser.getSelectedFiles()) {
                    try {
                        String nombre = archivo.getName().toLowerCase();
                        if (nombre.endsWith(".png") || nombre.endsWith(".jpg")) {
                            Image img = ImageIO.read(archivo);
                            ImageIcon icon = new ImageIcon(img.getScaledInstance(dimension, dimension, Image.SCALE_SMOOTH));
                            imagenesPersonalizadas.add(icon);
                        }
                    } catch (Exception ex) {
                        System.out.println("No se pudo cargar: " + archivo.getName());
                    }
                }
                modo = "Images";
                actualizarEstado();
                habilitarPlay();
            }
        });

        cancelBtn.addActionListener(e -> dialogo.dispose());

        botones.add(tipBtn);
        botones.add(mineBtn);
        botones.add(cancelBtn);
        dialogo.add(mensaje, BorderLayout.CENTER);
        dialogo.add(botones, BorderLayout.SOUTH);
        dialogo.setVisible(true);
    }

    static void mostrarManual() {
        String texto = "1. Selecciona el tamaño del tablero: 4x4 o 6x6.\n"
                + "2. Elige cómo jugar:\n"
                + "   - Números: busca pares de números.\n"
                + "   - Letras: busca pares de letras.\n"
                + "   - Mis imágenes: sube tus propias imágenes.\n"
                + "   - Imágenes sugeridas: usa las del juego.\n"
                + "3. Haz clic en Play para iniciar.\n"
                + "4. Encuentra todos los pares haciendo clic en las casillas.\n"
                + "5. Usa Reset para reiniciar.";
        JOptionPane.showMessageDialog(ventana, texto, "Manual de uso", JOptionPane.INFORMATION_MESSAGE);
    }

    static void mostrarAbout() {
        String texto = "Memorito - Juego de memoria educativo y personalizable.\n\n"
                + "Diseñado por Wing como parte de un proyecto de aprendizaje en Java.\n"
                + "Desarrollado con Java Swing.\n"
                + "Versión inicial: 2025\n\n"
                + "Licencia libre para fines educativos.";
        JOptionPane.showMessageDialog(ventana, texto, "Acerca de Memorito", JOptionPane.INFORMATION_MESSAGE);
    }
}
