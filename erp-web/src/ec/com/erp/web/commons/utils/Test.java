package ec.com.erp.web.commons.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Test {

	

	public static void main(String args[]) {
		impresora p = new impresora();
		p.setDispositivo("Bullzip PDF Printer");
		p.escribir((char) 27 + "m");
		p.setTipoCaracterLatino();
		p.setRojo();
		p.escribir("Esto es una prueba");
		p.setNegro();
		p.escribir("esto es negro" + (char) 130);
		p.setFormato(24);
		p.escribir("esto es negro con formato");
		p.setFormato(1);
		p.escribir("esto es negro con formato");
		p.correr(10);
		p.cortar();
		p.cerrarDispositivo();
	}

	// public static void main(String[] args) {
	// try {
	//
	// String texto = "Esto es lo que va a la impresora";
	// PrintService printService =
	// PrintServiceLookup.lookupDefaultPrintService();
	// DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
	// DocPrintJob docPrintJob = printService.createPrintJob();
	// Doc doc = new SimpleDoc(texto.getBytes(), flavor, null);
	// try {
	// docPrintJob.print(doc, null);
	// } catch (PrintException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	// } catch (Exception e) {
	// System.out.println("Error: " + e.getMessage());
	// }
	//
	//
	// try {
	// int margin = 0;
	// String output = "tighgh";
	// JTextPane jtp = new JTextPane();
	// jtp.setText(output);
	// jtp.setFont(new Font(Font.MONOSPACED, 0 , 8));
	// PrinterJob printerJob = PrinterJob.getPrinterJob();
	// PageFormat pageFormat = printerJob.defaultPage();
	// Paper paper = new Paper();
	// //paper.setSize(180.0, (double) (paper.getHeight() + lines * 10.0));
	// paper.setImageableArea(margin, margin, paper.getWidth() - margin * 2,
	// paper.getHeight() - margin * 2);
	// pageFormat.setPaper(paper);
	// pageFormat.setOrientation(PageFormat.PORTRAIT);
	// printerJob.setPrintable(jtp.getPrintable(null, null), pageFormat);
	// printerJob.print();
	// } catch (Exception e) {
	// System.out.println(e);
	// }
	// }
}

class impresora{
	// Variables de acceso al dispositivo
		private FileWriter fw;
		private BufferedWriter bw;
		private PrintWriter pw;
		private String dispositivo;

		// Esta funcion inicia el dispositivo donde se va a imprimir
		public void setDispositivo(String texto) {
			dispositivo = texto;
			if (texto.trim().length() <= 0) {// Si el dispositivo viene en blanco el
												// sistema tratara de definirlo

				// Session misession=new Session();
				// dispositivo=misession.impresora_tiquets();
				if (dispositivo.trim().length() <= 0) {

					dispositivo = "LPT1";// Esto si es windows

					// debido a que la clase session no se encuentra subida quiten
					// el if() y solo dejenlo con el puerto a usar
					// ya sea para windows o Linux funciona bien, lo probe
				}

			}
			try {
				fw = new FileWriter(dispositivo);
				bw = new BufferedWriter(fw);
				pw = new PrintWriter(bw);
			} catch (Exception e) {
				System.out.print(e);
			}

		}

		public void escribir(String texto) {
			try {

				pw.println(texto);

			} catch (Exception e) {
				System.out.print(e);
			}

		}

		public  void cortar( ) {
		try{

		char[] ESC_CUT_PAPER = new char[] { 0x1B, 'm'};
		if(!this.dispositivo.trim().equals("pantalla.txt")){
		pw.write(ESC_CUT_PAPER);
		}

		}catch(Exception e){
		System.out.print(e);
		}

		}

		public void avanza_pagina() {
			try {
				if (!this.dispositivo.trim().equals("pantalla.txt")) {
					pw.write(0x0C);
				}

			} catch (Exception e) {
				System.out.print(e);
			}

		}

		public  void setRojo( ) {
		try{
		char[] ESC_CUT_PAPER = new char[] { 0x1B, 'r',1};
		if(!this.dispositivo.trim().equals("pantalla.txt")){
		pw.write(ESC_CUT_PAPER);
		}

		}catch(Exception e){
		System.out.print(e);
		}

		}

		public  void setNegro( ) {
		try{
		char[] ESC_CUT_PAPER = new char[] { 0x1B, 'r',0};
		if(!this.dispositivo.trim().equals("pantalla.txt")){
		pw.write(ESC_CUT_PAPER);
		}

		}catch(Exception e){
		System.out.print(e);
		}
		}

		public  void setTipoCaracterLatino( ) {
		try{
		char[] ESC_CUT_PAPER = new char[] { 0x1B, 'R',18};
		if(!this.dispositivo.trim().equals("pantalla.txt")){
		pw.write(ESC_CUT_PAPER);
		}

		}catch(Exception e){
		System.out.print(e);
		}
		}

		public  void setFormato(int formato ) {
		try{
		char[] ESC_CUT_PAPER = new char[] { 0x1B, '!',(char)formato};
		if(!this.dispositivo.trim().equals("pantalla.txt")){
		pw.write(ESC_CUT_PAPER);
		}

		}catch(Exception e){
		System.out.print(e);
		}
		}

		public void correr(int fin) {
			try {
				int i = 0;
				for (i = 1; i <= fin; i++) {
					this.salto();
				}

			} catch (Exception e) {
				System.out.print(e);
			}
		}

		public void salto() {
			try {

				pw.println("");

			} catch (Exception e) {
				System.out.print(e);

			}

		}

		public void dividir() {
			escribir("—————————————-");

		}

		public void cerrarDispositivo() {
			try {

				pw.close();
				if (this.dispositivo.trim().equals("pantalla.txt")) {

					java.io.File archivo = new java.io.File("pantalla.txt");
					java.awt.Desktop.getDesktop().open(archivo);
				}

			} catch (Exception e) {
				System.out.print(e);
			}

		}
}
