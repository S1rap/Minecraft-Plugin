package plugin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

public class ItemSerialization {
	public String serializeItem_Stack(ItemStack im) {
		String itemStackString = "";
		try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(im);
            dataOutput.close();
            itemStackString = Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to save item stacks.", ex);
        }
		return itemStackString;
	}
	
	public ItemStack deserializeItem_Stack(String Item_String ) {
		ItemStack itemtoreturn = new ItemStack(Material.AIR);
        
        //decode the string back to an itemstack
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(Item_String));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            itemtoreturn = (ItemStack) dataInput.readObject();
            dataInput.close();
        } catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        return itemtoreturn;
	}
	
	
	public static String serializeInventory(Inventory inv) {
		String InvBackString = "";
		try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(inv);
            dataOutput.close();
            InvBackString = Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to save Inventory.", ex);
        }
		return InvBackString;
	}
	 
	public static Inventory deserializeInventory(String inv_string ) {
		Inventory invtoreturn = null;
        
        //decode the string back to an itemstack
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(inv_string));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            invtoreturn = (Inventory) dataInput.readObject();
            dataInput.close();
        } catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        return invtoreturn;
	}
}
