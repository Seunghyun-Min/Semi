package co.kr.necohost.semi.app.user;

import co.kr.necohost.semi.domain.model.entity.Menu;
import co.kr.necohost.semi.domain.service.CategoryService;
import co.kr.necohost.semi.domain.service.MenuService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
public class UserController {
    CategoryService categoryService;
    MenuService menuService;

    public UserController(CategoryService categoryService, MenuService menuService) {
        this.categoryService = categoryService;
        this.menuService = menuService;
    }

    @RequestMapping("/")
    public String index(Model model, HttpSession session) {
        model.addAttribute("session", session);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "user/home.html";
    }

    @RequestMapping("/order/menu")
    public String orderMenu(Model model, HttpSession session, @RequestParam Map<String, Object> params) {
        int category = Integer.parseInt(params.get("category").toString());
        if(params.get("kiosk") != null && !params.get("kiosk").toString().isEmpty()){
            model.addAttribute("isKiosk","true");
        }else{
            model.addAttribute("isKiosk","false");
        }
        model.addAttribute("session", session);
        model.addAttribute("menus",menuService.getMenuByCategory(category));
        model.addAttribute("categories", categoryService.getAllCategories());
        return "user/menu.html";
    }

}
