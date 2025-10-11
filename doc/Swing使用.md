

# JButton使用



```java
JButton btn = new JButton("111");
btn.setAction(new AbstractAction() {
  @Override
  public void actionPerformed(ActionEvent e) {

  }
});
```
显示效果如下

![image-20251011233311322](./assets/image-20251011233311322.png)

解决办法：

```java
JButton btn = new JButton("111");
btn.addMouseListener(new MouseAdapter() {
  @Override
  public void mouseClicked(MouseEvent e) {

  }
});
```