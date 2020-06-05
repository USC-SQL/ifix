# IFix: A Tool for the Automated Repair of Internationalization Presentation Failures (IPFs)

Internationalization enables companies to reach a global audience by adapting their websites to locale specific language and content. However, such translations can often introduce Internationalization Presentation Failures (IPFs) --- distortions in the intended appearance of a website. It is challenging for developers to design websites that can inherently adapt to varying lengths of text from different languages. Debugging and repairing IPFs is complicated by the large number of HTML elements and CSS properties that define a web page’s appearance. Tool support is also limited as existing techniques can only detect IPFs, with the repair remaining a labor intensive manual task. To address this problem, we propose, *IFix*, a search-based technique for automatically repairing IPFs in web applications. More algorithmic details of IFix can be found in our paper:
```
Automated Repair of Internationalization Failures in Web Applications Using Style Similarity Clustering and Search-Based Techniques
Sonal Mahajan, Abdulmajeed Alameer, Phil McMinn, William G. J. Halfond
In Proceedings of the 11th IEEE International Conference on Software Testing, Verification and Validation (ICST). April 2018. Acceptance rate: 25%. (To appear)
```
## Evaluation Data
#### Subjects:
The 23 real-world web pages used in the evaluation of IFix can be found [here](https://github.com/USC-SQL/ifix/tree/master/subjects).

#### User Study Data:
The surveys used in the user study can be found [here](https://github.com/USC-SQL/ifix/tree/master/surveys)

#### Results:
The evaluation and user study can be found [here](https://github.com/USC-SQL/ifix/tree/master/data)

## The journal paper replication package can be found [here](https://github.com/USC-SQL/ifix/tree/master/journal-replication-package)

## Code
The code in this repository represents the extended version of IFix (IFix++).
In order to run IFix++, you need to download and configure [GWALI](https://github.com/USC-SQL/gwali) in the same workspace

## Questions
In case of any questions you can email at halfond [at] usc {dot} edu
