import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.*;

class Product {
    int id;
    String name;
    String cat;
    double price;

    Product(int id, String name, String cat, double price){
        this.id = id;
        this.name = name;
        this.cat = cat;
        this.price = price;
    }

    public String toString(){
        return "[" + cat + "] " + id + " - " + name + " (₹" + price + ")";
    }
}

class CartItem {
    Product p;
    int q;

    CartItem(Product p, int q){
        this.p = p;
        this.q = q;
    }

    double total(){
        return p.price * q;
    }
}

public class MiniCart extends JFrame {

    DefaultListModel<Product> products = new DefaultListModel<>();
    java.util.List<CartItem> cart = new ArrayList<>();

    JTable table = new JTable(new DefaultTableModel(
            new Object[]{"ID","Name","Qty","Price","Line"},0){
        public boolean isCellEditable(int r,int c){ return false; }
    });

    JTextField qtyField = new JTextField("1",4);
    JTextField couponField = new JTextField(8);

    JLabel subLbl = new JLabel("Subtotal: ₹0.00");
    JLabel disLbl = new JLabel("Discount: -₹0.00");
    JLabel totLbl = new JLabel("Total: ₹0.00");

    Map<String,Integer> coupons = new LinkedHashMap<>();
    String applied = "";

    DecimalFormat df = new DecimalFormat("#,##0.00");

    MiniCart(){

        super("Mini Shopping Cart");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(820,480);
        setLocationRelativeTo(null);

        seedProducts();
        seedCoupons();

        DefaultListModel<String> display = new DefaultListModel<>();

        for(int i=0;i<products.size();i++){
            display.addElement(products.get(i).toString());
        }

        JList<String> list = new JList<>(display);

        JButton addBtn = new JButton("Add");

        addBtn.addActionListener(e->{
            int idx = list.getSelectedIndex();
            if(idx<0){
                msg("Select a product");
                return;
            }

            int q = parse(qtyField.getText());
            if(q<=0){
                msg("Enter valid qty");
                return;
            }

            Product p = products.get(idx);
            addToCart(p,q);
            refresh();
        });

        JScrollPane center = new JScrollPane(table);

        JButton removeBtn = new JButton("Remove Selected");

        removeBtn.addActionListener(e->{
            int row = table.getSelectedRow();
            if(row<0){
                msg("Select cart row");
                return;
            }

            int pid = Integer.parseInt(table.getValueAt(row,0).toString());
            cart.removeIf(ci->ci.p.id==pid);
            refresh();
        });

        JButton checkout = new JButton("Checkout");

        checkout.addActionListener(e->checkout());

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(list),center);

        split.setResizeWeight(0.4);

        add(split,BorderLayout.CENTER);
        add(checkout,BorderLayout.SOUTH);

        refresh();
    }

    void seedProducts(){

        products.addElement(new Product(101,"Smartphone","Electronics",15999));
        products.addElement(new Product(102,"Laptop","Electronics",49999));
        products.addElement(new Product(103,"Headphones","Electronics",1899));
        products.addElement(new Product(201,"T-Shirt","Clothing",499));
        products.addElement(new Product(202,"Jeans","Clothing",1499));
    }

    void seedCoupons(){
        coupons.put("IND10",10);
        coupons.put("SAVE5",5);
    }

    void addToCart(Product p,int q){

        for(CartItem ci: cart){
            if(ci.p.id==p.id){
                ci.q = ci.q + q;
                return;
            }
        }

        cart.add(new CartItem(p,q));
    }

    void refresh(){

        DefaultTableModel m = (DefaultTableModel) table.getModel();
        m.setRowCount(0);

        for(CartItem ci: cart){
            m.addRow(new Object[]{
                    ci.p.id,
                    ci.p.name,
                    ci.q,
                    "₹"+df.format(ci.p.price),
                    "₹"+df.format(ci.total())
            });
        }

        refreshTotals();
    }

    void refreshTotals(){

        double sub=0;

        for(CartItem ci: cart){
            sub += ci.total();
        }

        int pct = coupons.getOrDefault(applied,0);

        double dis = sub*pct/100;
        double tot = sub-dis;

        subLbl.setText("Subtotal: ₹"+df.format(sub));
        disLbl.setText("Discount: -₹"+df.format(dis));
        totLbl.setText("Total: ₹"+df.format(tot));
    }

    void checkout(){

        if(cart.isEmpty()){
            msg("Cart is empty");
            return;
        }

        try{
            PrintWriter pw = new PrintWriter(new FileWriter("receipt.txt"));

            for(CartItem ci: cart){
                pw.println(ci.p.name + " x" + ci.q + " = ₹" + ci.total());
            }

            pw.close();

        }catch(Exception e){
            msg("Error writing receipt");
        }

        cart.clear();
        refresh();
    }

    int parse(String s){
        try{
            return Integer.parseInt(s.trim());
        }catch(Exception e){
            return -1;
        }
    }

    void msg(String s){
        JOptionPane.showMessageDialog(this,s);
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> new MiniCart().setVisible(true));
    }
}
