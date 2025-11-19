import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class LunchRandom {

    public static void main(String[] args) {

        ArrayList<String> menus = new ArrayList<>();
        menus.add("김치찌개");
        menus.add("된장찌개");
        menus.add("냉면");
        menus.add("햄버거");
        menus.add("제육볶음");
        menus.add("초밥");
        menus.add("치킨");
        menus.add("피자");

        Scanner sc = new Scanner(System.in);
        Random random = new Random();

        while (true) {
            System.out.println("\n====== 랜덤 점심 메뉴 추천기 ====== ");
            System.out.println("현재 메뉴 : " + menus);
            System.out.println("1. 메뉴 추천");
            System.out.println("2. 메뉴 추가");
            System.out.println("3. 메뉴 삭제");
            System.out.println("4. 종료");
            System.out.print("번호 선택 >> ");

            int choice = sc.nextInt();
            sc.nextLine(); // 버퍼 비우기

            switch (choice) {
                case 1: // 메뉴 추천
                    if (menus.isEmpty()) {
                        System.out.println("⚠ 메뉴가 비어 있습니다! 먼저 추가해주세요.");
                    } else {
                        String pick = menus.get(random.nextInt(menus.size()));
                        System.out.println("👉 오늘 점심 추천 메뉴: **" + pick + "** 😋");
                    }
                    break;

                case 2: // 메뉴 추가
                    System.out.print("추가할 메뉴 입력 >> ");
                    String addMenu = sc.nextLine();
                    if (!addMenu.trim().isEmpty()) {
                        menus.add(addMenu);
                        System.out.println("✅ '" + addMenu + "' 메뉴가 추가되었습니다.");
                    }
                    break;

                case 3: // 메뉴 삭제
                    System.out.print("삭제할 메뉴 입력 >> ");
                    String delMenu = sc.nextLine();
                    if (menus.contains(delMenu)) {
                        menus.remove(delMenu);
                        System.out.println("🗑 '" + delMenu + "' 메뉴가 삭제되었습니다.");
                    } else {
                        System.out.println("해당 메뉴가 목록에 없습니다.");
                    }
                    break;

                case 4: // 종료
                    System.out.println("프로그램 종료! 맛있는 점심 드세요 😄");
                    sc.close();
                    return;

                default:
                    System.out.println("번호를 다시 입력해주세요.");
            }
        }
    }
}
