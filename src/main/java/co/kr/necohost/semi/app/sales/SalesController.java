package co.kr.necohost.semi.app.sales;

import co.kr.necohost.semi.domain.model.dto.SalesRequest;
import co.kr.necohost.semi.domain.model.entity.Sales;
import co.kr.necohost.semi.domain.service.SalesService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class SalesController {

    private final SalesService salesService;

    public SalesController(SalesService salesService) {
        this.salesService = salesService;
    }

    // 구버전 관리자 판매 메뉴 페이지를 반환하는 메서드(css없음)
    @RequestMapping(value = "/adminSalesMenu", method=RequestMethod.GET)
    public String getOldAdminSalesMenu() {
        return "/sales/adminSalesMenu.html";
    }

    // 신버전 관리자 페이지 - 판매 관리 페이지를 반환하는 메서드(css있음)
    @RequestMapping(value = "/adminSalesMain", method=RequestMethod.GET)
    public String getAdminSalesMenu() {
        return "/sales/adminSalesMain.html";
    }




    // 판매 데이터베이스 컨트롤러 메뉴 페이지를 반환하는 메서드
    @RequestMapping(value = "/salesDataBaseControllerMenu", method=RequestMethod.GET)
    public String getSalesDataBaseControllerMenu() {
        return "/sales/salesDataBaseControllerMenu.html";
    }

    // 판매 생성 페이지를 반환하는 메서드 (GET 요청)
    @RequestMapping(value="/createSales", method=RequestMethod.GET)
    public String getCreateSales(Model model) {
        model.addAttribute("salesRequest", new SalesRequest());
        return "/sales/createSales.html";
    }

    // 새로운 판매를 생성하는 메서드 (POST 요청)
    @RequestMapping(value="/createSales", method=RequestMethod.POST)
    public String postCreateSales(Model model, @ModelAttribute("salesRequest") SalesRequest salesRequest) {
        salesService.save(salesRequest);
        return "/sales/createSales.html";
    }

    // 판매 데이터를 읽어오는 메서드
    @RequestMapping(value="/readSales", method=RequestMethod.GET)
    public String getReadSales(Model model) {
        List<Sales> sales = salesService.findByProcess(1);
        model.addAttribute("sales", sales);
        model.addAttribute("salesRequest", new SalesRequest());
        return "/sales/readSales.html";
    }


    //6월 10일 작업중. 각 카테고리별 총 판매액을 반환하는 메서드
    @RequestMapping(value = "/totalSalesbyCategory", method = RequestMethod.GET)
    public String getTotalSalesByCategory(Model model) {
        Map<String, Double> totalByCategory = salesService.getTotalSalesByCategory();
        model.addAttribute("totalByCategory", totalByCategory);
        System.out.println(totalByCategory);
        return "/sales/totalSalesByCategory.html";
    }









    // 각 메뉴별 총 판매액을 반환하는 메서드
    @RequestMapping(value = "/totalSalesbyMenu", method = RequestMethod.GET)
    public String getTotalSalesByMenu(Model model) {
        Map<String, Double> totalByMenu = salesService.getTotalSalesByMenu();
        model.addAttribute("totalByMenu", totalByMenu);

        return "/sales/totalSalesByMenu.html";
    }

    // 월별 총 판매액을 반환하는 메서드
//    @RequestMapping(value="/totalSalesByMonth" , method = RequestMethod.GET)
//    public String getTotalSalesByMonth(Model model) {
//        Map<String, Double> monthlySales = salesService.getMonthlySalesByProcess();
//        Map<String, Double> sortedMonthlySales = new TreeMap<>(monthlySales); // TreeMap을 사용하여 정렬
//        model.addAttribute("monthlySales", sortedMonthlySales);
//        return "sales/totalSalesByMonth";
//    }





    // 연도별 총 판매액을 반환하는 메서드
//    @RequestMapping(value= "/totalSalesByYear", method = RequestMethod.GET)
//    public String getTotalSalesByYear(Model model) {
//        Map<Integer, Double> yearlySales = salesService.getYearlySalesByProcess();
//        model.addAttribute("yearlySales", yearlySales);
//        System.out.println(yearlySales);
//        return "/sales/totalSalesByYear";
//    }
    // 연도별 총 판매액을 반환하는 메서드. 6월 10일 오후 4시 수정중
    @RequestMapping(value = "/totalSalesByYear", method = RequestMethod.GET)
    public String getTotalSalesByYear(Model model) {
        Map<Integer, Double> yearlySales = salesService.getYearlySalesByProcess();

        // 연도별 오름차순으로 정렬
        Map<String, String> yearlySalesStringKey = yearlySales.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        entry -> entry.getKey().toString(),
                        entry -> String.format("%,d", entry.getValue().longValue()),
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        model.addAttribute("yearlySales", yearlySales);
        System.out.println(yearlySalesStringKey);
        return "sales/totalSalesByYear";
    }


    // 월별 총 판매액을 반환하는 메서드
//    @RequestMapping(value="/totalSalesByMonth" , method = RequestMethod.GET)
//    public String getTotalSalesByMonth(Model model) {
//        Map<String, Double> monthlySales = salesService.getMonthlySalesByProcess();
//        Map<String, Double> sortedMonthlySales = new TreeMap<>(monthlySales); // TreeMap을 사용하여 정렬
//        model.addAttribute("monthlySales", sortedMonthlySales);
//        return "sales/totalSalesByMonth";
//    }
    // 월별 총 판매액을 반환하는 메서드 6월 10일 수정중
    @RequestMapping(value = "/totalSalesByMonth", method = RequestMethod.GET)
    public String getTotalSalesByMonth(Model model) {
        Map<String, Double> monthlySales = salesService.getMonthlySalesByProcess();
        Map<String, String> formattedMonthlySales = monthlySales.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> String.format("%,d", entry.getValue().longValue()),
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
        model.addAttribute("monthlySales", monthlySales);
        model.addAttribute("formattedMonthlySales", formattedMonthlySales); // 포맷팅된 데이터를 따로 추가
        return "sales/totalSalesByMonth";
    }




    // 입력된 연도의 총 판매액을 반환하는 메서드
    @RequestMapping(value= "/totalSalesByYearInput", method = RequestMethod.GET)
    public String getTotalSalesByYearInput(@RequestParam(value = "year", required = false) Integer year, Model model) {
        if (year != null) {
            double totalSales = salesService.getTotalSalesByYear(year);
            model.addAttribute("year", year);
            model.addAttribute("totalSales", totalSales);
        }
        return "/sales/totalSalesByYearInput";
    }

    // 입력된 연도와 월의 총 판매액 및 성장률을 반환하는 메서드
    @RequestMapping(value="/totalSalesByYearAndMonthInput" , method = RequestMethod.GET)
    public String getTotalSalesByYearAndMonthInput(@RequestParam(value = "year", required = false) Integer year,
                                                   @RequestParam(value = "month", required = false) Integer month, Model model) {
        if (year != null && month != null) {
            double totalSales = salesService.getTotalSalesByYearAndMonth(year, month);
            double growthRate = salesService.getMonthlySalesGrowthRate(year, month);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            model.addAttribute("totalSales", totalSales);
            model.addAttribute("growthRate", growthRate);
        }
        return "/sales/totalSalesByYearAndMonthInput";
    }

    //입력된 날짜(연-월-일)의 총 판매액을 반환하는 메서드
    @RequestMapping(value = "/totalSalesByDayInput", method = RequestMethod.GET)
    public String getTotalSalesByDay(@RequestParam(value="year", required = false) Integer year,
                                     @RequestParam(value="month", required = false) Integer month,
                                     @RequestParam(value="day", required = false) Integer day,
                                     Model model) {

        if (year != null && month != null && day != null) {
            double totalSales = salesService.getTotalSalesByDay(year, month, day);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            model.addAttribute("day", day);
            model.addAttribute("totalSales", totalSales);
        }
        return "/sales/totalSalesByDayInput.html";
    }


    // 입력된 날짜(연-월-일)가 속한 주의 주별 매출(1주간 총매출, 요일별 매출)을 반환하는 메서드
    @RequestMapping(value = "/totalWeeklySalesByDayInput", method = RequestMethod.GET)
    public String getTotalWeeklySalesByDay(@RequestParam(value="year", required = false) Integer year,
                                           @RequestParam(value="month", required = false) Integer month,
                                           @RequestParam(value="day", required = false) Integer day,
                                           Model model) {

        if (year != null && month != null && day != null) {
            Map<LocalDate, Double> weeklySales = salesService.getWeeklySalesByDay(year, month, day);
            double totalWeeklySales = weeklySales.values().stream().mapToDouble(Double::doubleValue).sum();

            model.addAttribute("year", year);
            model.addAttribute("month", month);
            model.addAttribute("day", day);
            model.addAttribute("weeklySales", weeklySales);
            model.addAttribute("totalWeeklySales", totalWeeklySales);
        }
        return "/sales/totalWeeklySalesByDayInput";
    }






    // 입력된 연도와 카테고리의 총 판매액을 반환하는 메서드
    @RequestMapping(value="/totalSalesByYearAndCategoryInput" , method = RequestMethod.GET)
    public String getTotalSalesByYearAndCategoryInput(@RequestParam(value = "year", required = false) Integer year,
                                                      @RequestParam(value = "category", required = false) Integer category, Model model) {
        if (year != null && category != null) {
            double totalSales = salesService.getTotalSalesByYearAndCategory(year, category);
            model.addAttribute("year", year);
            model.addAttribute("category", category);
            model.addAttribute("totalSales", totalSales);
        }
        return "sales/totalSalesByYearAndCategoryInput";
    }




}
